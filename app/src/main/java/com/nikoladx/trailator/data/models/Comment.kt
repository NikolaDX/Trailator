package com.nikoladx.trailator.data.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Comment(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val text: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
)