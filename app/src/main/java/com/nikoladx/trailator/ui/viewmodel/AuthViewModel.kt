package com.nikoladx.trailator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikoladx.trailator.data.repository.AuthRepository
import com.nikoladx.trailator.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

data class ValidationState(
    val emailError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val repeatPasswordError: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _validationState = MutableStateFlow(ValidationState())
    val validationState: StateFlow<ValidationState> = _validationState.asStateFlow()

    val isUserAuthenticated: Boolean
        get() = repository.currentUser != null

    fun register(
        email: String,
        username: String,
        password: String,
        repeatPassword: String
    ) {
        // Validate inputs
        if (!validateRegistration(email, username, password, repeatPassword)) {
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            when (val result = repository.register(email, username, password)) {
                is Resource.Success -> {
                    _authState.value = AuthState(isSuccess = true)
                }
                is Resource.Error -> {
                    _authState.value = AuthState(error = result.message)
                }
                is Resource.Loading -> {
                    _authState.value = AuthState(isLoading = true)
                }
            }
        }
    }

    fun login(email: String, password: String) {
        // Validate inputs
        if (!validateLogin(email, password)) {
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            when (val result = repository.login(email, password)) {
                is Resource.Success -> {
                    _authState.value = AuthState(isSuccess = true)
                }
                is Resource.Error -> {
                    _authState.value = AuthState(error = result.message)
                }
                is Resource.Loading -> {
                    _authState.value = AuthState(isLoading = true)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _validationState.value = ValidationState(emailError = "Email is required")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _validationState.value = ValidationState(emailError = "Invalid email format")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState(isLoading = true)

            when (val result = repository.resetPassword(email)) {
                is Resource.Success -> {
                    _authState.value = AuthState(isSuccess = true)
                }
                is Resource.Error -> {
                    _authState.value = AuthState(error = result.message)
                }
                is Resource.Loading -> {
                    _authState.value = AuthState(isLoading = true)
                }
            }
        }
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }

    fun clearValidationErrors() {
        _validationState.value = ValidationState()
    }

    private fun validateRegistration(
        email: String,
        username: String,
        password: String,
        repeatPassword: String
    ): Boolean {
        var emailError: String? = null
        var usernameError: String? = null
        var passwordError: String? = null
        var repeatPasswordError: String? = null

        // Email validation
        when {
            email.isBlank() -> emailError = "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                emailError = "Invalid email format"
        }

        // Username validation
        when {
            username.isBlank() -> usernameError = "Username is required"
            username.length < 3 -> usernameError = "Username must be at least 3 characters"
            username.length > 20 -> usernameError = "Username must be less than 20 characters"
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) ->
                usernameError = "Username can only contain letters, numbers, and underscores"
        }

        // Password validation
        when {
            password.isBlank() -> passwordError = "Password is required"
            password.length < 6 -> passwordError = "Password must be at least 6 characters"
        }

        // Repeat password validation
        when {
            repeatPassword.isBlank() -> repeatPasswordError = "Please confirm your password"
            password != repeatPassword -> repeatPasswordError = "Passwords do not match"
        }

        _validationState.value = ValidationState(
            emailError = emailError,
            usernameError = usernameError,
            passwordError = passwordError,
            repeatPasswordError = repeatPasswordError
        )

        return emailError == null && usernameError == null &&
                passwordError == null && repeatPasswordError == null
    }

    private fun validateLogin(email: String, password: String): Boolean {
        var emailError: String? = null
        var passwordError: String? = null

        // Email validation
        when {
            email.isBlank() -> emailError = "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                emailError = "Invalid email format"
        }

        // Password validation
        if (password.isBlank()) {
            passwordError = "Password is required"
        }

        _validationState.value = ValidationState(
            emailError = emailError,
            passwordError = passwordError
        )

        return emailError == null && passwordError == null
    }
}