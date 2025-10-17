package com.nikoladx.trailator.ui.screens.home.maps

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.nikoladx.trailator.ui.screens.home.maps.components.AddObjectDialog
import com.nikoladx.trailator.ui.screens.home.maps.components.FilterDialog
import com.nikoladx.trailator.ui.objects.ObjectDetailsBottomSheet
import com.nikoladx.trailator.ui.screens.home.maps.viewmodels.MapViewModel
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.Circle
import com.nikoladx.trailator.R
import com.nikoladx.trailator.ui.screens.home.maps.components.NotificationPermissionDialog
import com.nikoladx.trailator.ui.screens.home.maps.components.RadiusSlider
import com.nikoladx.trailator.ui.screens.home.maps.utils.MarkerUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsScreen(
    viewModel: MapViewModel = viewModel(),
    userId: String,
    onRequestNotificationPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    // State management
    val cameraPositionState = rememberCameraPositionState()
    val context = LocalContext.current
    var mapLoaded by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onPermissionGranted()
        } else {
            viewModel.onPermissionDenied()
        }
    }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    // Map
    val markerBitmaps = remember { mutableMapOf<String, BitmapDescriptor>() }
    val position = LatLng(
        uiState.location.latitude,
        uiState.location.longitude
    )
    val retroMapStyle = remember {
        MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_retro)
    }
    var tempRadius by remember { mutableFloatStateOf(uiState.searchRadius) }
    val animatedRadius by animateFloatAsState(
        targetValue = tempRadius,
        animationSpec = tween(durationMillis = 100)
    )

    // Launched Effects
    LaunchedEffect(Unit) {
        if (!uiState.permissionGranted) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!viewModel.hasNotificationPermission()) {
            showPermissionDialog = true
        }
    }

    LaunchedEffect(mapLoaded, uiState.location) {
        if (mapLoaded && uiState.location.latitude != 0.0) {
            val cameraPosition = CameraPosition.fromLatLngZoom(uiState.location, 1f)

            val isDefaultPosition = cameraPositionState.position.target.run {
                latitude == 0.0 && longitude == 0.0
            }

            if (!cameraPositionState.isMoving || isDefaultPosition) {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newCameraPosition(cameraPosition),
                    durationMs = 1000
                )
            }
        }
    }

    if (!uiState.permissionGranted) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Location access must be enabled")
        }
        return
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
            ) {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = true,
                        mapStyleOptions = retroMapStyle
                    ),
                    onMapLoaded = { mapLoaded = true }
                ) {
                    if (animatedRadius > 0) {
                        Circle(
                            center = position,
                            radius = animatedRadius.toDouble(),
                            strokeWidth = 2f,
                            strokeColor = MaterialTheme.colorScheme.primary,
                            fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    }

                    uiState.trailObjects.forEach { obj ->
                        val objectPosition = LatLng(
                            obj.location.latitude,
                            obj.location.longitude
                        )
                        val objectMarkerState = remember(obj.id) { MarkerState(objectPosition) }
                        var markerIcon by remember(obj.id) {
                            mutableStateOf<BitmapDescriptor?>(null)
                        }

                        LaunchedEffect(obj.id, obj.photoUrls.firstOrNull()) {
                            val cachedBitmap = markerBitmaps[obj.id]
                            if (cachedBitmap != null) {
                                markerIcon = cachedBitmap
                            } else {
                                val bitmap = MarkerUtils.createMarkerBitmap(
                                    context = context,
                                    imageUrl = obj.photoUrls.firstOrNull(),
                                    type = obj.type
                                )
                                markerBitmaps[obj.id] = bitmap
                                markerIcon = bitmap
                            }
                        }

                        markerIcon?.let { icon ->
                            Marker(
                                state = objectMarkerState,
                                title = obj.title,
                                snippet = "${obj.type.name} by ${obj.authorName}",
                                icon = icon,
                                onClick = {
                                    viewModel.selectObject(obj)
                                    true
                                }
                            )
                        }
                    }
                }

                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.error?.let { error ->
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        Text(error)
                    }
                }
            }

            // Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showFilterDialog = true }
                ) {
                    Icon(Icons.Filled.FilterList, "Filter")
                }

                RadiusSlider(
                    modifier = Modifier.weight(1f).padding(12.dp),
                    radius = tempRadius,
                    onRadiusChange = { newRadius ->
                        tempRadius = newRadius
                    },
                    onUpdateFinished = {
                        viewModel.updateSearchRadius(tempRadius)
                    }
                )

                Button(
                    onClick = { viewModel.showAddObjectDialog() }
                ) {
                    Icon(Icons.Filled.Add, "Add object")
                }
            }
        }
    }

    if (showPermissionDialog) {
        NotificationPermissionDialog(
            onRequestPermission = {
                showPermissionDialog = false
                onRequestNotificationPermission()
            },
            onDismiss = {
                showPermissionDialog = false
            },
            onOpenSettings = {
                showPermissionDialog = false
                onOpenSettings()
            }
        )
    }

    if (uiState.showAddObjectDialog) {
        AddObjectDialog(
            currentLocation = viewModel.getCurrentLocationAsGeoPoint(),
            userId = userId,
            onDismiss = { viewModel.hideAddObjectDialog() },
            onObjectAdded = { viewModel.hideAddObjectDialog() },
            trailObjectRepository = viewModel.repository
        )
    }

    if (showFilterDialog) {
        FilterDialog(
            currentFilter = uiState.currentFilter.copy(radiusInMeters = null),
            onDismiss = { showFilterDialog = false },
            onApplyFilter = { filter ->
                val filterWithRadius = filter.copy(
                    radiusInMeters = uiState.searchRadius.toDouble().takeIf { it > 0 },
                    centerLocation = viewModel.getCurrentLocationAsGeoPoint()
                        .takeIf { uiState.searchRadius > 0 }
                )
                viewModel.applyFilter(filterWithRadius)
                showFilterDialog = false
            }
        )
    }

    uiState.selectedObject?.let { obj ->
        ObjectDetailsBottomSheet(
            trailObject = obj,
            userId = userId,
            onDismiss = { viewModel.selectObject(null) },
            onRate = { rating -> viewModel.addRating(obj.id, userId, rating) },
            onComment = { text, userName ->
                viewModel.addComment(obj.id, userId, userName, text)
            }
        )
    }
}