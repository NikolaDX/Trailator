package com.nikoladx.trailator.ui.screens.home.viewmodels

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.nikoladx.trailator.data.models.LocationState
import com.nikoladx.trailator.data.models.TrailObject
import com.nikoladx.trailator.data.models.TrailObjectFilter
import com.nikoladx.trailator.data.repositories.TrailObjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapUiState(
    val permissionGranted: Boolean = false,
    val location: LatLng = LatLng(0.0, 0.0),
    val trailObjects: List<TrailObject> = emptyList(),
    val selectedObject: TrailObject? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddObjectDialog: Boolean = false,
    val currentFilter: TrailObjectFilter = TrailObjectFilter()
)

class MapViewModel(val repository: TrailObjectRepository, application: Application) : AndroidViewModel(application) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _locationState = MutableStateFlow(LocationState())
    val locationState = _locationState.asStateFlow()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                _locationState.update {
                    it.copy(location = LatLng(location.latitude, location.longitude))
                }
            }
        }
    }

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        5000L
    ).apply {
        setMinUpdateIntervalMillis(2000L)
        setWaitForAccurateLocation(true)
    }.build()

    init {
        checkAndStartLocationUpdates()
        loadTrailObjects()

        viewModelScope.launch {
            _locationState.collect { locationState ->
                _uiState.update {
                    it.copy(
                        location = locationState.location,
                        permissionGranted = locationState.permissionGranted
                    )
                }
            }
        }
    }

    private fun checkAndStartLocationUpdates() {
        val isGranted = ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        _locationState.update { it.copy(permissionGranted = isGranted) }

        if (isGranted) {
            startLocationUpdates()
        }
    }

    fun onPermissionGranted() {
        _locationState.update { it.copy(permissionGranted = true) }
        _uiState.update { it.copy(permissionGranted = true) }
        startLocationUpdates()
    }

    fun onPermissionDenied() {
        _locationState.update { it.copy(permissionGranted = false) }
        _uiState.update { it.copy(permissionGranted = false) }
        stopLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (!_locationState.value.permissionGranted) return

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            _locationState.update { it.copy(permissionGranted = false) }
            e.printStackTrace()
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun loadTrailObjects() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.getFilteredTrailObjects(_uiState.value.currentFilter)
                .collect { objects ->
                    _uiState.update {
                        it.copy(
                            trailObjects = objects,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }


    fun applyFilter(filter: TrailObjectFilter) {
        _uiState.update { it.copy(currentFilter = filter) }
        loadTrailObjects()
    }

    fun selectObject(trailObject: TrailObject?) {
        _uiState.update { it.copy(selectedObject = trailObject) }
    }

    fun showAddObjectDialog() {
        _uiState.update { it.copy(showAddObjectDialog = true) }
    }

    fun hideAddObjectDialog() {
        _uiState.update { it.copy(showAddObjectDialog = false) }
    }

    fun addRating(objectId: String, userId: String, rating: Int) {
        viewModelScope.launch {
            repository.addRating(objectId, userId, rating).onSuccess {
                loadTrailObjects()
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun addComment(objectId: String, userId: String, userName: String, text: String) {
        viewModelScope.launch {
            repository.addComment(objectId, userId, userName, text).onSuccess {
                loadTrailObjects()
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun getCurrentLocationAsGeoPoint(): GeoPoint {
        val loc = _uiState.value.location
        return GeoPoint(loc.latitude, loc.longitude)
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}