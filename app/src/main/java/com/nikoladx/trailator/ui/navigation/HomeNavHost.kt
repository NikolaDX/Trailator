package com.nikoladx.trailator.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nikoladx.trailator.data.repositories.AuthenticationRepositoryImpl
import com.nikoladx.trailator.services.firebase.FirebaseAuthService
import com.nikoladx.trailator.services.firebase.FirebaseUserService
import com.nikoladx.trailator.ui.screens.home.FeedScreen
import com.nikoladx.trailator.ui.screens.home.MapsScreen
import com.nikoladx.trailator.ui.screens.home.ProfileScreen
import com.nikoladx.trailator.ui.screens.home.RankingsScreen
import com.nikoladx.trailator.ui.screens.home.viewmodels.MapViewModelFactory

private val authService = FirebaseAuthService()
private val userService = FirebaseUserService()
private val authRepository = AuthenticationRepositoryImpl(authService, userService)

@Composable
fun HomeNavHost(navController: NavHostController, modifier: Modifier) {
    val application = LocalContext.current.applicationContext as Application
    val currentUserId = authRepository.getPersistedUserUid() ?: "guest_user_id"

    NavHost(
        navController = navController,
        startDestination = HomeTab.Feed.route,
        modifier = modifier
    ) {
        composable(HomeTab.Feed.route) { FeedScreen() }
        composable(HomeTab.Maps.route) {
            val mapViewModelFactory = MapViewModelFactory(application)
            MapsScreen(
                viewModel = viewModel(factory = mapViewModelFactory),
                userId = currentUserId
            )
        }
        composable(HomeTab.Rankings.route) { RankingsScreen() }
        composable(HomeTab.Profile.route) { ProfileScreen() }
    }
}