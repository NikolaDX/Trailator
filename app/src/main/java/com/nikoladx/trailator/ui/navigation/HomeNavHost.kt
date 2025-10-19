package com.nikoladx.trailator.ui.navigation

import android.Manifest
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nikoladx.trailator.data.models.HomeTopBarContent
import com.nikoladx.trailator.data.repositories.AuthenticationRepositoryImpl
import com.nikoladx.trailator.services.firebase.FirebaseAuthService
import com.nikoladx.trailator.services.firebase.FirebaseUserService
import com.nikoladx.trailator.ui.screens.home.FeedScreen
import com.nikoladx.trailator.ui.screens.home.maps.MapsScreen
import com.nikoladx.trailator.ui.screens.home.profile.ProfileScreen
import com.nikoladx.trailator.ui.screens.home.leaderboard.RankingsScreen
import com.nikoladx.trailator.ui.screens.home.maps.viewmodels.MapViewModelFactory
import com.nikoladx.trailator.ui.screens.home.profile.viewmodels.ProfileViewModelFactory
import com.nikoladx.trailator.ui.screens.home.leaderboard.viewmodels.RankingsViewModelFactory

private val authService = FirebaseAuthService()
private val userService = FirebaseUserService()
private val authRepository = AuthenticationRepositoryImpl(authService, userService)

@Composable
fun HomeNavHost(
    navController: NavHostController,
    modifier: Modifier,
    onUpdateTopBar: (HomeTopBarContent) -> Unit,
    onAccountDeleted: () -> Unit
) {
    val application = LocalContext.current.applicationContext as Application
    val currentUserId = authRepository.getPersistedUserUid() ?: "guest_user_id"
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    NavHost(
        navController = navController,
        startDestination = HomeTab.Feed.route,
        modifier = modifier
    ) {
        // Feed Tab
        composable(HomeTab.Feed.route) {
            onUpdateTopBar(HomeTopBarContent(title = HomeTab.Feed.title))
            FeedScreen()
        }

        // Maps Tab
        composable(HomeTab.Maps.route) {
            onUpdateTopBar(HomeTopBarContent(title = HomeTab.Maps.title))
            val mapViewModelFactory = MapViewModelFactory(application)
            MapsScreen(
                viewModel = viewModel(factory = mapViewModelFactory),
                userId = currentUserId,
                onRequestNotificationPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
                onOpenSettings = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                onNavigateToProfile = { userId ->
                    navController.navigate(Routes.viewProfile(userId))
                }
            )
        }

        // Leaderboard Tab
        composable(HomeTab.Leaderboard.route) {
            onUpdateTopBar(HomeTopBarContent(title = HomeTab.Leaderboard.title))
            val rankingsViewModelFactory = RankingsViewModelFactory(application)
            RankingsScreen(viewModel(factory = rankingsViewModelFactory))
        }

        // Profile Tab
        composable(HomeTab.Profile.route) {
            val profileViewModelFactory = ProfileViewModelFactory(application, currentUserId)
            var profileActions by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

            onUpdateTopBar(
                HomeTopBarContent(
                    title = HomeTab.Profile.title,
                    actions = profileActions
                )
            )

            ProfileScreen(
                viewModel = viewModel(factory = profileViewModelFactory),
                loggedInUserId = currentUserId,
                targetUserId = currentUserId,
                onSetTopBarActions = { isEditing, onSave, onEdit, canEdit ->
                    profileActions = {
                        when {
                            isEditing -> {
                                TextButton(onClick = onSave) {
                                    Text("Save")
                                }
                            }
                            canEdit -> {
                                IconButton(onClick = onEdit) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                            }
                        }
                    }
                },
                onAccountDeleted = onAccountDeleted
            )
        }


        composable(
            route = Routes.VIEW_PROFILE,
            arguments = listOf(
                navArgument(Routes.VIEW_PROFILE_ARG) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val targetUserId = backStackEntry.arguments?.getString(Routes.VIEW_PROFILE_ARG)
                ?: currentUserId

            val profileViewModelFactory = ProfileViewModelFactory(application, targetUserId)
            var profileActions by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

            onUpdateTopBar(
                HomeTopBarContent(
                    title = "User Profile",
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = profileActions
                )
            )

            ProfileScreen(
                viewModel = viewModel(factory = profileViewModelFactory),
                loggedInUserId = currentUserId,
                targetUserId = targetUserId,
                onSetTopBarActions = { isEditing, onSave, onEdit, canEdit ->
                    if (currentUserId == targetUserId) {
                        profileActions = {
                            when {
                                isEditing -> {
                                    TextButton(onClick = onSave) { Text("Save") }
                                }
                                canEdit -> {
                                    IconButton(onClick = onEdit) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                }
                            }
                        }
                    } else {
                        profileActions = { }
                    }
                },
                onAccountDeleted = onAccountDeleted
            )
        }
    }
}