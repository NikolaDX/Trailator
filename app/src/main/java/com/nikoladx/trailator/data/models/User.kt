package com.nikoladx.trailator.data.models
import kotlinx.serialization.Serializable

@Serializable
data class User(
    var uid: String = "",
    var email: String = "",
    var name: String = "",
    var lastName: String = "",
    var imageUri: String = ""
)