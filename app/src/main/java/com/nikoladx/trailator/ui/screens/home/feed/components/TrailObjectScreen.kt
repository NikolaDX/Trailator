package com.nikoladx.trailator.ui.screens.home.feed.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nikoladx.trailator.ui.screens.home.maps.components.ObjectDetailsBottomSheet
import com.nikoladx.trailator.ui.screens.home.maps.viewmodels.MapViewModel

@Composable
fun TrailObjectScreen(
    objectId: String,
    userId: String,
    onNavigateToProfile: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: MapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load specific trail object
    LaunchedEffect(objectId) {
        viewModel.loadTrailObjectById(objectId)
    }

    Box(modifier = Modifier.fillMaxHeight()) {
        val trailObject = uiState.selectedObject
        if (trailObject != null) {
            ObjectDetailsBottomSheet(
                trailObject = trailObject,
                userId = userId,
                onDismiss = onBack,
                onRate = { rating -> viewModel.addRating(trailObject.id, userId, rating) },
                onComment = { comment -> viewModel.addComment(trailObject.id, userId, comment) },
                onDelete = { id -> viewModel.deleteTrailObject(id, userId) },
                viewModel = viewModel,
                onNavigateToProfile = onNavigateToProfile
            )
        } else if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Trail object not found.")
            }
        }
    }
}
