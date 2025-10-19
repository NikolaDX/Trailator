package com.nikoladx.trailator.ui.screens.home.profile.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikoladx.trailator.data.models.User
import com.nikoladx.trailator.data.repositories.AuthenticationRepository
import com.nikoladx.trailator.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: User = User(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthenticationRepository,
    private val userId: String
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            userRepository.getUser(userId).onSuccess { user ->
                _uiState.update { it.copy(user = user, isLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = "Failed to load profile: ${e.message}") }
            }
        }
    }

    fun saveProfile(
        name: String,
        lastName: String,
        newImageUri: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving  = true, error = null, successMessage = null) }

            if (name.isBlank() || lastName.isBlank()) {
                _uiState.update { it.copy(isSaving = false, error = "Name and last name cannot be empty.") }
                return@launch
            }

            userRepository.updateUserProfile(
                userId,
                name,
                lastName,
                newImageUri
            ).onSuccess {
                _uiState.update { it.copy(isSaving = false, successMessage = "Profile updated successfully!") }
                loadUserProfile()
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, error = "Failed to save profile: ${e.message}") }
            }
        }
    }

    fun dismissSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun refreshUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            userRepository.getUser(userId).onSuccess { user ->
                _uiState.update { it.copy(user = user, isLoading = false) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = "Failed to load profile: ${e.message}") }
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            authRepository.deleteAccount(userId).onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to delete account: ${exception.message}"
                    )
                }
            }
        }
    }
}