package com.nikoladx.trailator.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nikoladx.trailator.ui.screens.authentication.CreateAccountScreen
import com.nikoladx.trailator.ui.screens.authentication.SignInScreen
import com.nikoladx.trailator.ui.screens.home.HomeScreen

@Composable
fun AuthNavHost(modifier: Modifier, navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "signIn"
    ) {
        composable("signIn") {
            SignInScreen(
                modifier = modifier,
                onSignInSuccess = { navController.navigate("home") },
                onNavigateToSignUp = { navController.navigate("signUp") },
                onForgotPassword = { /* TODO: Forgot password screen */ }
            )
        }

        composable("signUp") {
            CreateAccountScreen(
                modifier = modifier,
                onAccountCreated = {
                    navController.popBackStack()
                }
            )
        }

        composable("home") {
            HomeScreen(modifier = modifier)
        }
    }
}