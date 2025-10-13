package com.nikoladx.trailator.data.models

import com.google.android.gms.maps.model.LatLng

data class LocationState(
    val location: LatLng = LatLng(43.321445, 21.896104),
    val permissionGranted: Boolean = false
)