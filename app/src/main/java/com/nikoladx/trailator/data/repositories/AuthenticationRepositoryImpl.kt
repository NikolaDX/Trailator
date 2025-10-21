package com.nikoladx.trailator.data.repositories

import com.nikoladx.trailator.data.models.User
import com.nikoladx.trailator.services.firebase.FirebaseAuthService
import com.nikoladx.trailator.services.firebase.FirebaseUserService

class AuthenticationRepositoryImpl(
    private val authService: FirebaseAuthService,
    private val userService: FirebaseUserService
): AuthenticationRepository {
    override suspend fun signUp(
        email: String,
        password: String,
        name: String,
        lastName: String,
        imageUri: String
    ): Result<User> {
        return try {
            val authResult = authService.signUp(email, password).getOrThrow()
            val firebaseUser = authResult.user

            if (firebaseUser == null) {
                authService.deleteCurrentUser()
                return Result.failure(Exception("User object is null."))
            }

            val newUser = User(
                uid = firebaseUser.uid,
                email = email,
                name = name,
                lastName = lastName,
                imageUri = imageUri
            )

            val saveResult = userService.addNewUser(newUser)

            if (saveResult.isFailure) {
                authService.deleteCurrentUser()
                return Result.failure(
                    saveResult.exceptionOrNull() ?: Exception("Failed to save user data.")
                )
            }

            Result.success(newUser)
        } catch (e: Exception) {
            authService.deleteCurrentUser()
            Result.failure(e)
        }
    }

    override suspend fun signIn(
        email: String,
        password: String
    ): Result<User> {
        return try {
            val authResult = authService.signIn(email, password).getOrThrow()
            val firebaseUser = authResult.user

            if (firebaseUser == null) {
                return Result.failure(Exception("User object is null."))
            }

            val userResult = userService.getUserById(firebaseUser.uid)

            if (userResult.isFailure) {
                return Result.failure(
                    userResult.exceptionOrNull() ?: Exception("Failed to fetch user data.")
                )
            }

            val user = userResult.getOrNull()

            if (user == null) {
                return Result.failure(Exception("User data not found."))
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun signOut() {
        authService.signOut()
    }

    override fun getPersistedUserUid(): String? {
        return authService.getCurrentUserUid()
    }

    override suspend fun deleteAccount(userId: String): Result<Unit> {
        return try {
            val deleteAuthResult = authService.deleteCurrentUser()
            if (deleteAuthResult.isFailure) {
                return Result.failure(
                    deleteAuthResult.exceptionOrNull()
                        ?: Exception("Failed to delete authentication")
                )
            }

            val deleteDataResult = userService.deleteUserData(userId)
            if (deleteDataResult.isFailure) {
                return Result.failure(
                    deleteDataResult.exceptionOrNull()
                        ?: Exception("Failed to delete user data")
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}