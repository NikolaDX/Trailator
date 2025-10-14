package com.nikoladx.trailator.data.models

import java.util.Date

data class TrailObjectFilter(
    val authorId: String? = null,
    val types: List<TrailObjectType>? = null,
    val minRating: Double? = null,
    val tags: List<String>? = null,
    val dateFrom: Date? = null,
    val dateTo: Date? = null,
    val difficulty: TrailDifficulty? = null,
    val searchQuery: String? = null
)