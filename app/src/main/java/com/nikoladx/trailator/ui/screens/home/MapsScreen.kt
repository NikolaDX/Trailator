package com.nikoladx.trailator.ui.screens.home

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.nikoladx.trailator.data.models.TrailObjectType
import com.nikoladx.trailator.ui.objects.AddObjectDialog
import com.nikoladx.trailator.ui.objects.FilterDialog
import com.nikoladx.trailator.ui.objects.ObjectDetailsBottomSheet
import com.nikoladx.trailator.ui.screens.home.viewmodels.MapViewModel
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.MapStyleOptions
import com.nikoladx.trailator.R


@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsScreen(
    viewModel: MapViewModel = viewModel(),
    userId: String
) {
    val context = LocalContext.current
    val retroMapStyle = remember {
        MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_retro)
    }
    val uiState by viewModel.uiState.collectAsState()
    val cameraPositionState = rememberCameraPositionState()
    var showFilterDialog by remember { mutableStateOf(false) }
    var mapLoaded by remember { mutableStateOf(false) }
    val position = com.google.android.gms.maps.model.LatLng(
        uiState.location.latitude,
        uiState.location.longitude
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onPermissionGranted()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        if (!uiState.permissionGranted) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(mapLoaded, uiState.location) {
        if (mapLoaded && uiState.location.latitude != 0.0) {
            val cameraPosition = CameraPosition.fromLatLngZoom(uiState.location, 15f)

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

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = { showFilterDialog = true }
                ) {
                    Icon(Icons.Default.FilterList, "Filter")
                }

                FloatingActionButton(
                    onClick = { viewModel.showAddObjectDialog() }
                ) {
                    Icon(Icons.Default.Add, "Add object")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = true,
                    mapStyleOptions = retroMapStyle
                ),
                onMapLoaded = { mapLoaded = true }
            ) {
                uiState.trailObjects.forEach { obj ->
                    val objectPosition = com.google.android.gms.maps.model.LatLng(
                        obj.location.latitude,
                        obj.location.longitude
                    )
                    val objectMarkerState = remember(obj.id) { MarkerState(objectPosition) }

                    Marker(
                        state = objectMarkerState,
                        title = obj.title,
                        snippet = "${obj.type.name} by ${obj.authorName}",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            getMarkerColor(obj.type)
                        ),
                        onClick = {
                            viewModel.selectObject(obj)
                            true
                        }
                    )
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
            currentFilter = uiState.currentFilter,
            onDismiss = { showFilterDialog = false },
            onApplyFilter = { filter ->
                viewModel.applyFilter(filter)
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

private fun getMarkerColor(type: TrailObjectType): Float {
    return when (type) {
        TrailObjectType.TRAIL -> BitmapDescriptorFactory.HUE_GREEN
        TrailObjectType.VIEWPOINT -> BitmapDescriptorFactory.HUE_VIOLET
        TrailObjectType.WATER_SOURCE -> BitmapDescriptorFactory.HUE_CYAN
        TrailObjectType.SHELTER -> BitmapDescriptorFactory.HUE_ORANGE
        TrailObjectType.LANDMARK -> BitmapDescriptorFactory.HUE_YELLOW
        TrailObjectType.CAMPING_SPOT -> BitmapDescriptorFactory.HUE_ROSE
        TrailObjectType.DANGER_ZONE -> BitmapDescriptorFactory.HUE_RED
    }
}