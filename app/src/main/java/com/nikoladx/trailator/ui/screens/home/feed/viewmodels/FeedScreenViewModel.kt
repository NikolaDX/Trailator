package com.nikoladx.trailator.ui.screens.home.feed.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikoladx.trailator.data.models.TrailObject
import com.nikoladx.trailator.data.repositories.TrailObjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FeedUiState (
    val isLoading: Boolean = false,
    val trailObjects: List<TrailObject> = emptyList()
)

class FeedScreenViewModel(
    private val repository: TrailObjectRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        loadTrailObjects()
    }

    fun loadTrailObjects() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.getAllTrailObjects()
                .collect { objects ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            trailObjects = objects
                        )
                    }
                }
        }
    }
}