package com.nikoladx.trailator.ui.screens.home.feed

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nikoladx.trailator.data.models.TrailObject
import com.nikoladx.trailator.ui.screens.home.feed.components.TrailObjectGrid
import com.nikoladx.trailator.ui.screens.home.feed.components.TrailObjectList
import com.nikoladx.trailator.ui.screens.home.maps.components.ObjectDetailsBottomSheet
import com.nikoladx.trailator.ui.screens.home.maps.viewmodels.MapViewModel
import kotlinx.coroutines.launch

enum class ScreenLayout { LIST, GRID }

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun rememberScreenLayout(): ScreenLayout {
    return if (LocalConfiguration.current.screenWidthDp.dp > 600.dp) {
        ScreenLayout.GRID
    } else {
        ScreenLayout.LIST
    }
}

@Composable
fun FeedScreen(
    userId: String,
    onNavigateToProfile: (String) -> Unit,
    mapViewModel: MapViewModel = viewModel()
) {
    val screenLayout = rememberScreenLayout()
    val state by mapViewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var selectedTrailObject by remember { mutableStateOf<TrailObject?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        mapViewModel.loadTrailObjects()
    }

    if (showBottomSheet && selectedTrailObject != null) {
        ObjectDetailsBottomSheet(
            trailObject = selectedTrailObject!!,
            userId = userId,
            onDismiss = {
                showBottomSheet = false
                selectedTrailObject = null
            },
            onRate = { rating ->
                mapViewModel.addRating(selectedTrailObject!!.id, userId, rating)
            },
            onComment = { comment ->
                coroutineScope.launch {
                    mapViewModel.addComment(selectedTrailObject!!.id, userId, comment)
                }
            },
            onDelete = { objectId ->
                coroutineScope.launch {
                    mapViewModel.deleteTrailObject(objectId, userId)
                    showBottomSheet = false
                }
            },
            viewModel = mapViewModel,
            onNavigateToProfile = onNavigateToProfile
        )
    }

    Scaffold { paddingValues ->
        when {
            state.isLoading -> LoadingState(paddingValues)
            state.trailObjects.isEmpty() -> EmptyState(paddingValues)
            else -> {
                val onItemClick: (String) -> Unit = { id ->
                    selectedTrailObject = state.trailObjects.find { it.id == id }
                    if (selectedTrailObject != null) {
                        showBottomSheet = true
                    }
                }

                if (screenLayout == ScreenLayout.GRID) {
                    TrailObjectGrid(
                        paddingValues = paddingValues,
                        trailObjects = state.trailObjects,
                        onTrailObjectClick = onItemClick
                    )
                } else {
                    TrailObjectList(
                        paddingValues = paddingValues,
                        trailObjects = state.trailObjects,
                        onTrailObjectClick = onItemClick
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingState(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyState(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Text("No trail objects found. Start exploring!")
    }
}