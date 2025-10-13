package com.nikoladx.trailator.ui.screens.home.viewmodels

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.nikoladx.trailator.data.models.LocationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

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
        startLocationUpdates()
    }

    fun onPermissionDenied() {
        _locationState.update { it.copy(permissionGranted = false) }
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

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}