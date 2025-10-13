package com.nikoladx.trailator.services.firebase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.nikoladx.trailator.data.models.User
import kotlinx.coroutines.tasks.await

class FirebaseUserService {
    private val db = Firebase.firestore

    suspend fun addNewUser(user: User): Result<Unit> {
        return try {
            db.collection("users")
                .document(user.uid)
                .set(user)
                .await()

            Log.d("Firestore", "User added with UID: ${user.uid}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("Firestore", "User adding failed", e)
            Result.failure(e)
        }
    }

    suspend fun getUserById(uid: String): Result<User> {
        return try {
            val document = db.collection("users")
                .document(uid)
                .get()
                .await()

            if (!document.exists()) {
                return Result.failure(Exception("User not found"))
            }

            val user = document.toObject(User::class.java)

            if (user == null) {
                return Result.failure(Exception("Failed to parse user data"))
            }

            Log.d("firestore", "User retrieved: ${user.uid}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e("Firestore", "Failed to get user", e)
            Result.failure(e)
        }
    }
}