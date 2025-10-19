package com.nikoladx.trailator.data.repositories

import com.google.firebase.firestore.GeoPoint
import com.nikoladx.trailator.data.models.TrailDifficulty
import com.nikoladx.trailator.data.models.TrailObject
import com.nikoladx.trailator.data.models.TrailObjectFilter
import com.nikoladx.trailator.data.models.TrailObjectType
import com.nikoladx.trailator.data.models.WaterQuality
import kotlinx.coroutines.flow.Flow

interface TrailObjectRepository {
    suspend fun addTrailObject(
        userId: String,
        type: TrailObjectType,
        title: String,
        description: String,
        location: GeoPoint,
        photoUris: List<String>,
        difficulty: TrailDifficulty? = null,
        waterQuality: WaterQuality? = null,
        capacity: Int? = null,
        elevation: Double? = null,
        tags: List<String> = emptyList()
    ): Result<String>

    suspend fun getAllTrailObjects(): Flow<List<TrailObject>>

    suspend fun getFilteredTrailObjects(filter: TrailObjectFilter): Flow<List<TrailObject>>

    suspend fun addRating(objectId: String, userId: String, rating: Int): Result<Unit>

    suspend fun addComment(
        objectId: String,
        userId: String,
        text: String
    ): Result<Unit>

    suspend fun deleteTrailObject(objectId: String, userId: String): Result<Unit>
}