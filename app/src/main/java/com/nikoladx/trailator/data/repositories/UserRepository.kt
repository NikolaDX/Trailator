package com.nikoladx.trailator.data.repositories

import com.google.firebase.firestore.GeoPoint
import com.nikoladx.trailator.data.models.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getRankedUsers(): Flow<List<User>>
    suspend fun getUser(userId: String): Result<User>
    suspend fun updateUserProfile(userId: String, name: String, lastName: String, imageUri: String?) : Result<Unit>
    suspend fun updateUserLocation(userId: String, location: GeoPoint): Result<Unit>
    fun getUserImageUriFlow(userId: String): Flow<String?>
    fun getUserName(userId: String): Flow<String?>
    suspend fun deleteAccount(userId: String): Result<Unit>
}