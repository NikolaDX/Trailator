package com.nikoladx.trailator.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nikoladx.trailator.data.models.User
import com.nikoladx.trailator.data.repository.AuthRepository
import com.nikoladx.trailator.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<Result<User>?>(null)
    val authState: StateFlow<Result<User>?> = _authState.asStateFlow()

    private val _isUserSignedIn = MutableStateFlow(authRepository.isUserSignedIn())
    val isUserSignedIn: StateFlow<Boolean> = _isUserSignedIn.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<Result<Unit>?>(null)
    val resetPasswordState: StateFlow<Result<Unit>?> = _resetPasswordState.asStateFlow()

    init {
        checkAuthStatus()
    }

    fun signUp(email: String, password: String, name: String, lastName: String) {
        if (!validateInput(email, password, name, lastName)) return

        viewModelScope.launch {
            _authState.value = Result.Loading
            _authState.value = authRepository.signUp(email, password, name, lastName)
            _isUserSignedIn.value = authRepository.isUserSignedIn()
        }
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = Result.Error("Email and password cannot be empty")
            return
        }

        viewModelScope.launch {
            _authState.value = Result.Loading
            _authState.value = authRepository.signIn(email, password)
            _isUserSignedIn.value = authRepository.isUserSignedIn()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _authState.value = null
            _isUserSignedIn.value = false
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _resetPasswordState.value = Result.Error("Email cannot be empty")
            return
        }

        viewModelScope.launch {
            _resetPasswordState.value = Result.Loading
            _resetPasswordState.value = authRepository.resetPassword(email)
        }
    }

    fun clearAuthState() {
        _authState.value = null
    }

    private fun checkAuthStatus() {
        _isUserSignedIn.value = authRepository.isUserSignedIn()
    }

    private fun validateInput(
        email: String,
        password: String,
        name: String,
        lastName: String
    ): Boolean {
        return when {
            email.isBlank() -> {
                _authState.value = Result.Error("Email cannot be empty")
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _authState.value = Result.Error("Invalid email format")
                false
            }
            password.isBlank() -> {
                _authState.value = Result.Error("Password cannot be empty")
                false
            }
            password.length < 6 -> {
                _authState.value = Result.Error("Password must be at least 6 characters")
                false
            }
            name.isBlank() -> {
                _authState.value = Result.Error("Name cannot be empty")
                false
            }
            lastName.isBlank() -> {
                _authState.value = Result.Error("Last name cannot be empty")
                false
            }
            else -> true
        }
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}