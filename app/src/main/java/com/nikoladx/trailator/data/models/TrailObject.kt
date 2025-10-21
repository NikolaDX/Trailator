package com.nikoladx.trailator.data.models

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class TrailObjectType {
    TRAIL,
    VIEWPOINT,
    WATER_SOURCE,
    SHELTER,
    LANDMARK,
    CAMPING_SPOT,
    DANGER_ZONE
}

enum class TrailDifficulty {
    EASY, MODERATE, HARD, EXTREME
}

enum class WaterQuality {
    POTABLE,
    NON_POTABLE,
    NEEDS_FILTER
}


data class TrailObject(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val type: TrailObjectType = TrailObjectType.TRAIL,
    val title: String = "",
    val description: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val photoUrls: List<String> = emptyList(),
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val lastInteractionAt: Date? = null,
    val ratings: Map<String, Int> = emptyMap(),
    val comments: List<Comment> = emptyList(),
    val difficulty: TrailDifficulty? = null,
    val waterQuality: WaterQuality? = null,
    val capacity: Int? = null,
    val elevation: Double? = null,
    val tags: List<String> = emptyList()
) {
    fun getAverageRating(): Double {
        return if (ratings.isEmpty()) 0.0
        else ratings.values.average()
    }
}