package com.nikoladx.trailator.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val profileImageUrl: String? = null,
    val createdAt: Long = 0L,
    val bio: String? = null
)