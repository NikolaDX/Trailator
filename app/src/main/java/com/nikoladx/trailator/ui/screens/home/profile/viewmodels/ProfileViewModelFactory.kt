package com.nikoladx.trailator.ui.screens.home.profile.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nikoladx.trailator.data.repositories.AuthenticationRepositoryImpl
import com.nikoladx.trailator.data.repositories.TrailObjectRepositoryImpl
import com.nikoladx.trailator.data.repositories.UserRepositoryImpl
import com.nikoladx.trailator.services.firebase.FirebaseAuthService
import com.nikoladx.trailator.services.firebase.FirebaseUserService

@Suppress("UNCHECKED_CAST")
class ProfileViewModelFactory(
    private val application: Application,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            val userRepository = UserRepositoryImpl(context = application.applicationContext)
            val authService = FirebaseAuthService()
            val userService = FirebaseUserService()
            val authRepository = AuthenticationRepositoryImpl(authService, userService)
            val trailRepository = TrailObjectRepositoryImpl(application.applicationContext)
            return ProfileViewModel(userRepository, trailRepository, authRepository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}