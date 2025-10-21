package com.nikoladx.trailator.ui.screens.home.leaderboard.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nikoladx.trailator.data.repositories.UserRepositoryImpl

@Suppress("UNCHECKED_CAST")
class LeaderboardViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeaderboardViewModel::class.java)) {
            val userRepositoryImpl = UserRepositoryImpl(application.applicationContext)
            return LeaderboardViewModel(userRepository = userRepositoryImpl) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}