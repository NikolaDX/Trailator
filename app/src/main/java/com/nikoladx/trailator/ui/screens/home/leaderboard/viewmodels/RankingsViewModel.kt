package com.nikoladx.trailator.ui.screens.home.leaderboard.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikoladx.trailator.data.models.User
import com.nikoladx.trailator.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RankingsUiState(
    val rankedUsers: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class RankingsViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(RankingsUiState())
    val uiState: StateFlow<RankingsUiState> = _uiState.asStateFlow()

    init {
        loadRankings()
    }

    fun loadRankings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            userRepository.getRankedUsers().collect { users ->
                _uiState.update {
                    it.copy(rankedUsers = users, isLoading = false)
                }
            }
        }
    }
}