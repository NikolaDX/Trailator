package com.nikoladx.trailator.ui.screens.home.feed.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nikoladx.trailator.data.repositories.TrailObjectRepositoryImpl

@Suppress("UNCHECKED_CAST")
class FeedScreenViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FeedScreenViewModel::class.java)) {
            val repository = TrailObjectRepositoryImpl(application.applicationContext)
            return FeedScreenViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}