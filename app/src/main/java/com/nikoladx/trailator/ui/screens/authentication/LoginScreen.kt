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
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToRegister: () -> Unit = {},
    onLoginSuccess: () -> Unit = {}
) {
    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val validationState by viewModel.validationState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle login success
    LaunchedEffect(authState.isSuccess) {
        if (authState.isSuccess) {
            onLoginSuccess()
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
                    "Trailator \uD83C\uDFD5\uFE0F",
                    style = MaterialTheme.typography.displayMedium
                )
                Text(
                    "Welcome back!",
                    style = MaterialTheme.typography.displaySmall
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

            Button(
                onClick = {
                    viewModel.login(
                        email = emailText,
                        password = passwordText
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
                Text("Log in")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "No account?",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Register now",
                    modifier = Modifier.clickable {
                        onNavigateToRegister()
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