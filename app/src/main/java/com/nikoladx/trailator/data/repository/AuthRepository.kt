package com.nikoladx.trailator.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.nikoladx.trailator.data.models.User
import com.nikoladx.trailator.util.Result
import kotlinx.coroutines.tasks.await

class AuthRepository private constructor(
    private val auth: FirebaseAuth
) {
    companion object {
        @Volatile
        private var instance: AuthRepository? = null

        fun getInstance(auth: FirebaseAuth = FirebaseAuth.getInstance()): AuthRepository {
            return instance ?: synchronized(this) {
                instance ?: AuthRepository(auth).also { instance = it }
            }
        }
    }
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signUp(
        email: String,
        password: String,
        name: String,
        lastName: String
    ): Result<User> = try {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user ?: throw Exception("User creation failed")

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName("$name $lastName")
            .build()
        firebaseUser.updateProfile(profileUpdates).await()

        Result.Success(
            User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: email,
                name = name,
                lastName = lastName
            )
        )
    } catch (e: Exception) {
        Result.Error(e.message ?: "Sign up failed")
    }

    suspend fun signIn(email: String, password: String): Result<User> = try {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user ?: throw Exception("Sign in failed")

        val displayName = firebaseUser.displayName?.split(" ") ?: listOf("", "")
        Result.Success(
            User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: email,
                name = displayName.firstOrNull() ?: "",
                lastName = displayName.getOrNull(1) ?: ""
            )
        )
    } catch (e: Exception) {
        Result.Error(e.message ?: "Sign in failed")
    }

    fun signOut(): Result<Unit> = try {
        auth.signOut()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Sign out failed")
    }

    suspend fun resetPassword(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Password reset failed")
    }

    fun isUserSignedIn(): Boolean = currentUser != null
}