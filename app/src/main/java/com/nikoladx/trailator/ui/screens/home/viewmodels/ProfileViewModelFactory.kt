package com.nikoladx.trailator.ui.screens.home.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nikoladx.trailator.data.repositories.UserRepositoryImpl
import com.nikoladx.trailator.services.firebase.FirebaseAuthService

@Suppress("UNCHECKED_CAST")
class ProfileViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            val userRepository = UserRepositoryImpl(context = application.applicationContext)
            val currentUserId = FirebaseAuthService().getCurrentUserUid()
            return ProfileViewModel(userRepository, currentUserId ?: "") as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}