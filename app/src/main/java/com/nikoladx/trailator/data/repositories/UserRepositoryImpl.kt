package com.nikoladx.trailator.data.repositories

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.nikoladx.trailator.data.models.User
import com.nikoladx.trailator.services.cloudinary.CloudinaryUploader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl(
    private val context: Context
): UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val cloudinaryUploader = CloudinaryUploader(context)

    override suspend fun getRankedUsers(): Flow<List<User>> = flow {
        try {
            val snapshot = usersCollection
                .orderBy("points", Query.Direction.DESCENDING)
                .get()
                .await()

            val users = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
            emit(users)
        } catch (_: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getUser(userId: String): Result<User> {
        return try {
            val snapshot = usersCollection.document(userId).get().await()
            val user = snapshot.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(NoSuchElementException("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(
        userId: String,
        name: String,
        lastName: String,
        imageUri: String?
    ): Result<Unit> {
        return try {
            var newImageUrl: String? = null

            if (!imageUri.isNullOrEmpty() && !imageUri.startsWith("http")) {
                newImageUrl = cloudinaryUploader.uploadImage(
                    imageUri,
                    folder = "user_profiles/$userId"
                )
            }

            val updates = mutableMapOf<String, Any>()
            updates["name"] = name
            updates["lastName"] = lastName

            if (newImageUrl != null) {
                updates["imageUri"] = newImageUrl
            }

            usersCollection.document(userId).update(updates).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserLocation(
        userId: String,
        location: GeoPoint
    ): Result<Unit> {
        return try {
            usersCollection.document(userId).update("location", location).await()
            Result.success(Unit)
        }  catch (e: Exception) {
            Result.failure(e)
        }
    }

}