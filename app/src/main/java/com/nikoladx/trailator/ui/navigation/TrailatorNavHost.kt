package com.nikoladx.trailator.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nikoladx.trailator.data.repositories.AuthenticationRepository
import com.nikoladx.trailator.ui.screens.authentication.LoginScreen
import com.nikoladx.trailator.ui.screens.authentication.RegisterScreen
import com.nikoladx.trailator.ui.screens.authentication.viewmodels.LoginViewModel
import com.nikoladx.trailator.ui.screens.authentication.viewmodels.LoginViewModelFactory
import com.nikoladx.trailator.ui.screens.authentication.viewmodels.RegisterViewModel
import com.nikoladx.trailator.ui.screens.authentication.viewmodels.RegisterViewModelFactory
import com.nikoladx.trailator.ui.screens.home.HomeScreen

@Composable
fun TrailatorNavHost(
    registerFactory: RegisterViewModelFactory,
    loginFactory: LoginViewModelFactory,
    authRepository: AuthenticationRepository
) {
    val navController = rememberNavController()

    val startDestination = remember {
        if (authRepository.getPersistedUserUid() != null) {
            Screen.Home.route
        } else {
            Screen.Login.route
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
        ) {
            composable(Screen.Login.route) {
                val viewModel = viewModel<LoginViewModel>(factory = loginFactory)
                LoginScreen(
                    modifier = Modifier.padding(innerPadding),
                    viewModel = viewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                val viewModel = viewModel<RegisterViewModel>(factory = registerFactory)
                RegisterScreen(
                    modifier = Modifier.padding(innerPadding),
                    viewModel = viewModel,
                    onRegistrationSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onSignOut = {
                        authRepository.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onAccountDeleted = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}