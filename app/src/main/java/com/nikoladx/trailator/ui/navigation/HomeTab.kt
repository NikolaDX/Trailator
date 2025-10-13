package com.nikoladx.trailator.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class HomeTab(val route: String, val title: String, val icon: ImageVector) {
    object Feed : HomeTab("feed", "Home", Icons.Filled.Home)
    object Maps : HomeTab("maps", "Maps", Icons.Filled.Face)
    object Rankings : HomeTab("rankings", "Rankings", Icons.Filled.Star)
    object Profile: HomeTab("profile", "Profile", Icons.Filled.Person)
}