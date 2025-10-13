package com.nikoladx.trailator.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nikoladx.trailator.ui.screens.home.FeedScreen
import com.nikoladx.trailator.ui.screens.home.MapsScreen
import com.nikoladx.trailator.ui.screens.home.ProfileScreen
import com.nikoladx.trailator.ui.screens.home.RankingsScreen

@Composable
fun HomeNavHost(navController: NavHostController, modifier: Modifier) {
    NavHost(
        navController = navController,
        startDestination = HomeTab.Feed.route,
        modifier = modifier
    ) {
        composable(HomeTab.Feed.route) { FeedScreen() }
        composable(HomeTab.Maps.route) { MapsScreen() }
        composable(HomeTab.Rankings.route) { RankingsScreen() }
        composable(HomeTab.Profile.route) { ProfileScreen() }
    }
}