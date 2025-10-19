package com.nikoladx.trailator.data.models

import java.util.Date

data class Comment(
    val id: String = "",
    val userId: String = "",
    val text: String = "",
    val timestamp: Date? = null
)