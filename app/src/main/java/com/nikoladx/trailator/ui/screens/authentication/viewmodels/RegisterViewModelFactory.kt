package com.nikoladx.trailator.ui.screens.authentication.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nikoladx.trailator.data.repositories.AuthenticationRepository
import com.nikoladx.trailator.services.cloudinary.CloudinaryUploader

class RegisterViewModelFactory(
    private val authRepository: AuthenticationRepository,
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            val cloudinaryUploader = CloudinaryUploader(application.applicationContext)
            return RegisterViewModel(authRepository, cloudinaryUploader) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}