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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nikoladx.trailator.data.models.HomeTopBarContent
import com.nikoladx.trailator.data.repositories.AuthenticationRepositoryImpl
import com.nikoladx.trailator.services.firebase.FirebaseAuthService
import com.nikoladx.trailator.services.firebase.FirebaseUserService
import com.nikoladx.trailator.ui.screens.home.FeedScreen
import com.nikoladx.trailator.ui.screens.home.maps.MapsScreen
import com.nikoladx.trailator.ui.screens.home.ProfileScreen
import com.nikoladx.trailator.ui.screens.home.RankingsScreen
import com.nikoladx.trailator.ui.screens.home.maps.viewmodels.MapViewModelFactory
import com.nikoladx.trailator.ui.screens.home.viewmodels.ProfileViewModelFactory
import com.nikoladx.trailator.ui.screens.home.viewmodels.RankingsViewModelFactory

private val authService = FirebaseAuthService()
private val userService = FirebaseUserService()
private val authRepository = AuthenticationRepositoryImpl(authService, userService)



@Composable
fun HomeNavHost(
    navController: NavHostController,
    modifier: Modifier,
    topBarContent: HomeTopBarContent,
    onUpdateTopBar: (HomeTopBarContent) -> Unit
) {
    val application = LocalContext.current.applicationContext as Application
    val currentUserId = authRepository.getPersistedUserUid() ?: "guest_user_id"
    var onEditAction: (() -> Unit)? by remember { mutableStateOf(null) }
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

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
        composable(HomeTab.Feed.route) {
            onUpdateTopBar(HomeTopBarContent(title = HomeTab.Feed.title))
            FeedScreen()
        }

        composable(HomeTab.Maps.route) {
            onUpdateTopBar(HomeTopBarContent(title = HomeTab.Maps.title))
            val mapViewModelFactory = MapViewModelFactory(application)
            MapsScreen(
                viewModel = viewModel(factory = mapViewModelFactory),
                userId = currentUserId,
                onRequestNotificationPermission = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        // Za starije verzije ne treba
                    }
                },
                onOpenSettings = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            )
        }

        composable(HomeTab.Rankings.route) {
            onUpdateTopBar(HomeTopBarContent(title = HomeTab.Rankings.title))
            val rankingsViewModelFactory = RankingsViewModelFactory(application)
            RankingsScreen(viewModel(factory = rankingsViewModelFactory))
        }

        composable(HomeTab.Profile.route) {
            val profileViewModelFactory = ProfileViewModelFactory(application)
            val currentUserId = FirebaseAuthService().getCurrentUserUid()

            var profileActions by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }

            onUpdateTopBar(
                HomeTopBarContent(
                    title = HomeTab.Profile.title,
                    actions = profileActions
                )
            )

            ProfileScreen(
                viewModel = viewModel(factory = profileViewModelFactory),
                currentUserId = currentUserId ?: "",
                onSetTopBarActions = { isEditing, onSave ->
                    profileActions = {
                        if (isEditing) {
                            TextButton(onClick = onSave) { Text("Save") }
                        } else {
                            IconButton(onClick = { onEditAction?.invoke() }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                        }
                    }
                },
                setOnEditAction = { onEditAction = it }
            )
        }
    }
}