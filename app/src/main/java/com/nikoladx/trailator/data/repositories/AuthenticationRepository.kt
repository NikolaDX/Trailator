package com.nikoladx.trailator.data.repositories

import com.nikoladx.trailator.data.models.User

interface AuthenticationRepository {
    suspend fun signUp(email: String, password: String, name: String, lastName: String, imageUri: String): Result<User>
    suspend fun signIn(email: String, password: String): Result<User>
    fun getPersistedUserUid(): String?
}