package com.nikoladx.trailator.data.models

data class User(
    val id: String,
    val email: String,
    val name: String,
    val lastName: String,
    val profileImageUrl: String? = null
)
