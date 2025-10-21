package com.nikoladx.trailator.ui.screens.home.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class HomeTab(val route: String, val title: String, val icon: ImageVector) {
    object Feed : HomeTab("feed", "Home", Icons.Filled.Home)
    object Maps : HomeTab("maps", "Maps", Icons.Filled.Map)
    object Leaderboard : HomeTab("leaderboard", "Leaderboard", Icons.Filled.Leaderboard)
    object Profile : HomeTab("profile", "Profile", Icons.Filled.Person)
}

object Routes {
    const val VIEW_PROFILE = "view_profile/{userId}"
    const val VIEW_PROFILE_ARG = "userId"

    const val VIEW_TRAIL_OBJECT = "view_trail_object/{objectId}"
    const val VIEW_TRAIL_OBJECT_ARG = "objectId"

    fun viewProfile(userId: String) = "view_profile/$userId"
    fun viewTrailObject(objectId: String) = "view_trail_object/$objectId"
}