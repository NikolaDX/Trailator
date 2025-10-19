package com.nikoladx.trailator.ui.screens.home.leaderboard.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nikoladx.trailator.data.repositories.UserRepositoryImpl

@Suppress("UNCHECKED_CAST")
class RankingsViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RankingsViewModel::class.java)) {
            val userRepositoryImpl = UserRepositoryImpl(application.applicationContext)
            return RankingsViewModel(userRepository = userRepositoryImpl) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}