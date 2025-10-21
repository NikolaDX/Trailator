package com.nikoladx.trailator.ui.screens.home.maps.viewmodels

import android.Manifest
import android.annotation.SuppressLint
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
import com.nikoladx.trailator.data.repositories.UserRepositoryImpl
import com.nikoladx.trailator.services.firebase.FirebaseAuthService
import com.nikoladx.trailator.services.notifications.NotificationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class MapUiState(
    val permissionGranted: Boolean = false,
    val location: LatLng = LatLng(0.0, 0.0),
    val trailObjects: List<TrailObject> = emptyList(),
    val selectedObject: TrailObject? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddObjectDialog: Boolean = false,
    val currentFilter: TrailObjectFilter = TrailObjectFilter(),
    val searchRadius: Float = 0f,
    val currentUserName: String = ""
)

private const val NEARBY_RADIUS_METERS = 1000.0

class MapViewModel(
    val repository: TrailObjectRepository,
    application: Application
) : AndroidViewModel(application) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _locationState = MutableStateFlow(LocationState())

    private val authService = FirebaseAuthService()
    private val userRepository = UserRepositoryImpl(application)

    private val notificationService = NotificationService(application)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                val newLocation = LatLng(location.latitude, location.longitude)
                _locationState.update {
                    it.copy(location = LatLng(location.latitude, location.longitude))
                }
                updateUserLocationInFirebase(newLocation.latitude, newLocation.longitude)
                checkForNearbyObjects(newLocation)
            }
        }
    }

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        2000L
    ).apply {
        setMinUpdateIntervalMillis(2000L)
        setWaitForAccurateLocation(true)
    }.build()

    init {
        checkAndStartLocationUpdates()
        loadTrailObjects()
        fetchCurrentUserName()

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

    private fun fetchCurrentUserName() {
        val userId = authService.getCurrentUserUid()
        if (userId != null) {
            viewModelScope.launch {
                try {
                    val userDoc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(userId)
                        .get()
                        .await()

                    val userName = userDoc.getString("name") ?: "Anonymous"
                    _uiState.update { it.copy(currentUserName = userName) }
                } catch (_: Exception) {
                    _uiState.update { it.copy(currentUserName = "Anonymous") }
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

    @Suppress("MissingPermission")
    private fun startLocationUpdates() {
        if (!_locationState.value.permissionGranted) return

        val hasPermission = ContextCompat.checkSelfPermission(
            getApplication(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            _locationState.update { it.copy(permissionGranted = false) }
            return
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            _locationState.update { it.copy(permissionGranted = false) }
            _uiState.update { it.copy(error = "Greška pri pristupanju lokaciji") }
            e.printStackTrace()
        }
    }

    private fun stopLocationUpdates() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadTrailObjects() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.getFilteredTrailObjects(_uiState.value.currentFilter)
                .collect { objects ->
                    val currentSelectedId = _uiState.value.selectedObject?.id
                    val updatedSelectedObject = if (currentSelectedId != null) {
                        objects.find { it.id == currentSelectedId }
                    } else {
                        null
                    }

                    _uiState.update {
                        it.copy(
                            trailObjects = objects,
                            selectedObject = updatedSelectedObject,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun loadTrailObjectById(objectId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val result = repository.getTrailObjectById(objectId)
            result.onSuccess { trailObject ->
                _uiState.update {
                    it.copy(
                        selectedObject = trailObject,
                        isLoading = false,
                        error = if (trailObject == null) "Object not found" else null
                    )
                }
            }.onFailure { exception ->
                _uiState.update { it.copy(isLoading = false, error = exception.message) }
            }
        }
    }

    fun deleteTrailObject(objectId: String, userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.deleteTrailObject(objectId, userId).onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedObject = null
                    )
                }
                loadTrailObjects()
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to delete object: ${exception.message}"
                    )
                }
            }
        }
    }

    fun applyFilter(filter: TrailObjectFilter) {
        val filterWithLocation = if (filter.radiusInMeters != null && filter.radiusInMeters > 0) {
            filter.copy(centerLocation = getCurrentLocationAsGeoPoint())
        } else {
            filter.copy(centerLocation = null)
        }

        _uiState.update { it.copy(currentFilter = filterWithLocation) }
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
        loadTrailObjects()
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

    fun addComment(objectId: String, userId: String, text: String) {
        viewModelScope.launch {
            repository.addComment(objectId, userId, text).onSuccess {
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

    fun getUserImageUriFlow(userId: String): Flow<String?> {
        return userRepository.getUserImageUriFlow(userId)
    }

    fun getUserName(userId: String): Flow<String?> {
        return userRepository.getUserName(userId)
    }

    fun updateSearchRadius(radius: Float) {
        val newRadius = radius.toDouble()
        _uiState.update { it.copy(searchRadius = radius) }

        val newCenterLocation = if (newRadius > 0) getCurrentLocationAsGeoPoint() else null
        val newRadiusInMeters = if (newRadius > 0) newRadius else null

        val newFilter = _uiState.value.currentFilter.copy(
            centerLocation = newCenterLocation,
            radiusInMeters = newRadiusInMeters
        )

        _uiState.update { it.copy(currentFilter = newFilter) }
        loadTrailObjects()
    }

    private fun updateUserLocationInFirebase(lat: Double, lon: Double) {
        val userId = authService.getCurrentUserUid()
        if (userId != null) {
            viewModelScope.launch {
                userRepository.updateUserLocation(userId, GeoPoint(lat, lon))
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkForNearbyObjects(currentLocation: LatLng) {
        val filter = TrailObjectFilter(
            centerLocation = GeoPoint(currentLocation.latitude, currentLocation.longitude),
            radiusInMeters = NEARBY_RADIUS_METERS
        )

        viewModelScope.launch {
            repository.getFilteredTrailObjects(filter)
                .collect { nearbyObjects ->
                    val userId = authService.getCurrentUserUid() ?: return@collect

                    if (nearbyObjects.isNotEmpty()) {
                        notificationService.showNearbyObjectsNotification(nearbyObjects)
                    } else {
                        notificationService.resetNotifiedObjects()
                    }

                    nearbyObjects.forEach { obj ->
                        viewModelScope.launch {
                            repository.awardVisitPoints(userId, obj.id)
                        }
                    }

                    return@collect
                }
        }
    }

    fun hasNotificationPermission(): Boolean {
        return notificationService.hasNotificationPermission()
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}