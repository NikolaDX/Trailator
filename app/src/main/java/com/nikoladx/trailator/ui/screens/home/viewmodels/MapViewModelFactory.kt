package com.nikoladx.trailator.ui.screens.home.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nikoladx.trailator.data.repositories.TrailObjectRepositoryImpl

@Suppress("UNCHECKED_CAST")
class MapViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            val repository = TrailObjectRepositoryImpl(application.applicationContext)
            return MapViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}