package com.nikoladx.trailator.data.repositories

import android.content.Context
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.nikoladx.trailator.data.models.Comment
import com.nikoladx.trailator.data.models.TrailDifficulty
import com.nikoladx.trailator.data.models.TrailObject
import com.nikoladx.trailator.data.models.TrailObjectFilter
import com.nikoladx.trailator.data.models.TrailObjectType
import com.nikoladx.trailator.data.models.WaterQuality
import com.nikoladx.trailator.services.cloudinary.CloudinaryUploader
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class TrailObjectRepositoryImpl(
    private val context: Context
) : TrailObjectRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val cloudinaryUploader = CloudinaryUploader(context)
    private val objectsCollection = firestore.collection("trail_objects")
    private val usersCollection = firestore.collection("users")

    override suspend fun addTrailObject(
        userId: String,
        type: TrailObjectType,
        title: String,
        description: String,
        location: GeoPoint,
        photoUris: List<String>,
        difficulty: TrailDifficulty?,
        waterQuality: WaterQuality?,
        capacity: Int?,
        elevation: Double?,
        tags: List<String>
    ): Result<String> {
        return try {
            val photoUrls = cloudinaryUploader.uploadMultipleImages(
                photoUris,
                folder = "trail_objects/$userId"
            )

            val userDoc = usersCollection.document(userId).get().await()
            val userName = userDoc.getString("name") ?: "Anonymous"

            val objectId = UUID.randomUUID().toString()
            val trailObject = TrailObject(
                id = objectId,
                authorId = userId,
                authorName = userName,
                type = type,
                title = title,
                description = description,
                location = location,
                photoUrls = photoUrls,
                difficulty = difficulty,
                waterQuality = waterQuality,
                capacity = capacity,
                elevation = elevation,
                tags = tags
            )

            objectsCollection.document(objectId).set(trailObject).await()
            awardPoints(userId, PointAction.ADD_OBJECT)
            Result.success(objectId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllTrailObjects(): Flow<List<TrailObject>> = flow {
        val snapshot = objectsCollection.get().await()
        val objects = snapshot.documents.mapNotNull { it.toObject(TrailObject::class.java) }
        emit(objects)
    }

    override suspend fun getFilteredTrailObjects(filter: TrailObjectFilter): Flow<List<TrailObject>> =
        flow {
            var query: Query = objectsCollection

            filter.authorId?.let {
                query = query.whereEqualTo("authorId", it)
            }

            filter.dateFrom?.let {
                query = query.whereGreaterThanOrEqualTo("createdAt", it)
            }

            filter.dateTo?.let {
                query = query.whereLessThanOrEqualTo("createdAt", it)
            }

            val snapshot = query.get().await()
            var objects = snapshot.documents.mapNotNull { it.toObject(TrailObject::class.java) }

            filter.centerLocation?.let { center ->
                filter.radiusInMeters?.let { radius ->
                    objects = objects.filter { obj ->
                        val distance = calculateDistance(
                            center.latitude,
                            center.longitude,
                            obj.location.latitude,
                            obj.location.longitude
                        )
                        distance <= radius
                    }
                }
            }

            filter.types?.let { types ->
                objects = objects.filter { it.type in types }
            }

            filter.minRating?.let { minRating ->
                objects = objects.filter { it.getAverageRating() >= minRating }
            }

            filter.tags?.let { tags ->
                objects = objects.filter { obj -> tags.any { it in obj.tags } }
            }

            filter.difficulty?.let { difficulty ->
                objects = objects.filter { it.difficulty == difficulty }
            }

            filter.searchQuery?.let { query ->
                objects = objects.filter {
                    it.title.contains(query, ignoreCase = true) || it.description.contains(
                        query,
                        ignoreCase = true
                    )
                }
            }

            emit(objects)
        }

    override suspend fun getTrailObjectById(objectId: String): Result<TrailObject?> {
        return try {
            val snapshot = objectsCollection.document(objectId).get().await()
            if (snapshot.exists()) {
                val trailObject = snapshot.toObject(TrailObject::class.java)
                Result.success(trailObject)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTrailObjectsByIds(ids: List<String>): Result<List<TrailObject>> {
        return try {
            val snapshot = firestore.collection("trail_objects")
                .whereIn("id", ids)
                .get()
                .await()
            val objects = snapshot.documents.mapNotNull { it.toObject(TrailObject::class.java) }
            Result.success(objects)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3
        val latRad1 = Math.toRadians(lat1)
        val latRad2 = Math.toRadians(lat2)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLon = Math.toRadians(lon2 - lon1)

        val a =
            sin(deltaLat / 2) * sin(deltaLat / 2) + cos(latRad1) * cos(latRad2) * sin(
                deltaLon / 2
            ) * sin(deltaLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }

    override suspend fun addRating(
        objectId: String,
        userId: String,
        rating: Int
    ): Result<Unit> {
        return try {
            val objectRef = objectsCollection.document(objectId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(objectRef)
                val ratings = snapshot.get("ratings") as? Map<String, Int> ?: emptyMap()
                val updatedRatings = ratings.toMutableMap().apply {
                    put(userId, rating)
                }

                transaction.update(
                    objectRef, mapOf(
                        "ratings" to updatedRatings,
                        "lastInteractionAt" to Date()
                    )
                )
            }.await()

            awardPoints(userId, PointAction.RATE_OBJECT)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addComment(
        objectId: String,
        userId: String,
        text: String
    ): Result<Unit> {
        return try {
            val comment = Comment(
                id = UUID.randomUUID().toString(),
                userId = userId,
                text = text,
                timestamp = Date()
            )

            val objectRef = objectsCollection.document(objectId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(objectRef)
                val comments =
                    (snapshot.get("comments") as? List<*>)?.mapNotNull { it as? Map<*, *> }
                        ?.map { map ->
                            Comment(
                                id = map["id"] as? String ?: "",
                                userId = map["userId"] as? String ?: "",
                                text = map["text"] as? String ?: "",
                                timestamp = (map["timestamp"] as? Timestamp)?.toDate()
                            )
                        } ?: emptyList()

                val updatedComments = comments + comment

                transaction.update(
                    objectRef, mapOf(
                        "comments" to updatedComments,
                        "lastInteractionAt" to Date()
                    )
                )
            }.await()

            awardPoints(userId, PointAction.COMMENT)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun awardPoints(userId: String, action: PointAction) {
        val userRef = usersCollection.document(userId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            var points = snapshot.getLong("points") ?: 0L
            var objectsAdded = snapshot.getLong("objectsAdded") ?: 0L
            var commentsPosted = snapshot.getLong("commentsPosted") ?: 0L
            var locationsVisited = snapshot.getLong("locationsVisited") ?: 0L

            points += action.points

            when (action) {
                PointAction.ADD_OBJECT -> objectsAdded++
                PointAction.COMMENT -> commentsPosted++
                PointAction.VISIT_LOCATION -> locationsVisited++
                else -> {}
            }

            val newRank = getRankBadge(points)
            val currentBadges = snapshot.get("achievedBadges") as? List<String> ?: emptyList()
            val newBadges = if (newRank !in currentBadges) currentBadges + newRank else currentBadges

            transaction.update(
                userRef,
                mapOf(
                    "points" to points,
                    "objectsAdded" to objectsAdded,
                    "commentsPosted" to commentsPosted,
                    "locationsVisited" to locationsVisited,
                    "rank" to newRank,
                    "achievedBadges" to newBadges
                )
            )
        }.await()
    }

    override suspend fun awardVisitPoints(userId: String, objectId: String) {
        val userRef = usersCollection.document(userId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val points = snapshot.getLong("points") ?: 0L
            val locationsVisited = snapshot.getLong("locationsVisited") ?: 0L
            val visitedIds = snapshot.get("visitedObjectIds") as? List<String> ?: emptyList()

            if (objectId !in visitedIds) {
                val newVisitedIds = visitedIds + objectId
                val newLocationsVisited = locationsVisited + 1
                val newPoints = points + PointAction.VISIT_LOCATION.points

                val newRank = getRankBadge(points)
                val currentBadges = snapshot.get("achievedBadges") as? List<String> ?: emptyList()
                val newBadges = if (newRank !in currentBadges) currentBadges + newRank else currentBadges

                transaction.update(
                    userRef,
                    mapOf(
                        "points" to newPoints,
                        "locationsVisited" to newLocationsVisited,
                        "visitedObjectIds" to newVisitedIds,
                        "rank" to newRank,
                        "achievedBadges" to newBadges
                    )
                )
            }
        }.await()
    }

    private fun getRankBadge(points: Long): String {
        return when {
            points >= 5000 -> "MASTER_EXPLORER"
            points >= 2000 -> "EXPERT_HIKER"
            points >= 1000 -> "ADVANCED_TREKKER"
            points >= 500 -> "TRAIL_SEEKER"
            points >= 100 -> "ENTHUSIAST"
            else -> "NOVICE"
        }
    }

    override suspend fun deleteTrailObject(
        objectId: String,
        userId: String
    ): Result<Unit> {
        return try {
            val objectRef = objectsCollection.document(objectId)
            val snapshot = objectRef.get().await()


            val authorId = snapshot.getString("authorId")
            if (authorId != userId) {
                return Result.failure(Exception("Unauthorized"))
            }

            objectRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

enum class PointAction(val points: Int) {
    ADD_OBJECT(50),
    RATE_OBJECT(5),
    COMMENT(10),
    VISIT_LOCATION(15)
}