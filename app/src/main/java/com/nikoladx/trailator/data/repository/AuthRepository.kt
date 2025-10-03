package com.nikoladx.trailator.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.nikoladx.trailator.data.model.User
import com.nikoladx.trailator.util.Resource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun register(
        email: String,
        username: String,
        password: String
    ): Resource<FirebaseUser> {
        return try {
            // Check if username already exists
            val usernameExists = checkUsernameExists(username)
            if (usernameExists) {
                return Resource.Error("Username already taken")
            }

            // Create user with email and password
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Resource.Error("User creation failed")

            // Update profile with username
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // Create user document in Firestore
            val user = User(
                uid = firebaseUser.uid,
                email = email,
                username = username,
                createdAt = System.currentTimeMillis()
            )
            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(user)
                .await()

            Resource.Success(firebaseUser)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Registration failed")
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Resource<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Resource.Error("Login failed")
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Login failed")
        }
    }

    suspend fun logout(): Resource<Unit> {
        return try {
            auth.signOut()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Logout failed")
        }
    }

    suspend fun resetPassword(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Password reset failed")
        }
    }

    private suspend fun checkUsernameExists(username: String): Boolean {
        return try {
            val result = firestore.collection("users")
                .whereEqualTo("username", username)
                .get()
                .await()
            !result.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getUserData(uid: String): Resource<User> {
        return try {
            val document = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            val user = document.toObject(User::class.java)
            if (user != null) {
                Resource.Success(user)
            } else {
                Resource.Error("User data not found")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch user data")
        }
    }
}