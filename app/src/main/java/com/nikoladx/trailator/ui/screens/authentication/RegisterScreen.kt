package com.nikoladx.trailator.ui.screens.authentication

import AppOutlinedTextField
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nikoladx.trailator.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit = {},
    onRegisterSuccess: () -> Unit = {}
) {
    var emailText by remember { mutableStateOf("") }
    var usernameText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var repeatPasswordText by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val validationState by viewModel.validationState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle registration success
    LaunchedEffect(authState.isSuccess) {
        if (authState.isSuccess) {
            onRegisterSuccess()
        }
    }

    // Handle errors
    LaunchedEffect(authState.error) {
        authState.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(error)
                viewModel.clearError()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                16.dp,
                alignment = Alignment.CenterVertically
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Create account",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    "Join the hiking community",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            AppOutlinedTextField(
                value = emailText,
                onValueChange = {
                    emailText = it
                    viewModel.clearValidationErrors()
                },
                labelText = "Enter your email",
                leadingIcon = Icons.Default.Email,
                singleLine = true,
                textKeyboardType = KeyboardType.Email,
                isPassword = false,
                isError = validationState.emailError != null,
                errorText = validationState.emailError
            )

            AppOutlinedTextField(
                value = usernameText,
                onValueChange = {
                    usernameText = it
                    viewModel.clearValidationErrors()
                },
                labelText = "Enter your username",
                leadingIcon = Icons.Default.Person,
                singleLine = true,
                textKeyboardType = KeyboardType.Text,
                isPassword = false,
                isError = validationState.usernameError != null,
                errorText = validationState.usernameError
            )

            AppOutlinedTextField(
                value = passwordText,
                onValueChange = {
                    passwordText = it
                    viewModel.clearValidationErrors()
                },
                labelText = "Enter your password",
                leadingIcon = Icons.Default.Lock,
                singleLine = true,
                textKeyboardType = KeyboardType.Password,
                isPassword = true,
                isError = validationState.passwordError != null,
                errorText = validationState.passwordError
            )

            AppOutlinedTextField(
                value = repeatPasswordText,
                onValueChange = {
                    repeatPasswordText = it
                    viewModel.clearValidationErrors()
                },
                labelText = "Repeat your password",
                leadingIcon = Icons.Default.Lock,
                singleLine = true,
                textKeyboardType = KeyboardType.Password,
                isPassword = true,
                isError = validationState.repeatPasswordError != null,
                errorText = validationState.repeatPasswordError
            )

            Button(
                onClick = {
                    viewModel.register(
                        email = emailText,
                        username = usernameText,
                        password = passwordText,
                        repeatPassword = repeatPasswordText
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !authState.isLoading
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text("Create account")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Already have an account?",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Log in",
                    modifier = Modifier.clickable {
                        onNavigateToLogin()
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}