package com.nikoladx.trailator.ui.screens.authentication.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikoladx.trailator.data.repositories.AuthenticationRepository
import com.nikoladx.trailator.services.cloudinary.CloudinaryUploader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val lastName: String = "",
    val imageUri: Uri? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistrationSuccessful: Boolean = false
)

class RegisterViewModel(
    private val authRepository: AuthenticationRepository,
    private val cloudinaryUploader: CloudinaryUploader
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, errorMessage = null) }
    }

    fun onLastNameChange(lastName: String) {
        _uiState.update { it.copy(lastName = lastName, errorMessage = null) }
    }

    fun onImageSelected(uri: Uri?) {
        _uiState.update { it.copy(imageUri = uri, errorMessage = null) }
    }

    fun onSignUpClick() {
        clearError()

        val currentState = _uiState.value

        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email is required") }
            return
        }

        if (currentState.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Password is required") }
            return
        }

        if (currentState.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters") }
            return
        }

        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name is required") }
            return
        }

        if (currentState.lastName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Last name is required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val imageUriString = currentState.imageUri?.toString() ?: ""

            val uploadedImageUrl = imageUriString.let { uri ->
                if (!uri.startsWith("http")) {
                    cloudinaryUploader.uploadImage(uri)
                } else uri
            }

            val result = authRepository.signUp(
                email = currentState.email,
                password = currentState.password,
                name = currentState.name,
                lastName = currentState.lastName,
                imageUri = uploadedImageUrl
            )

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRegistrationSuccessful = true,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Registration failed"
                        )
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}