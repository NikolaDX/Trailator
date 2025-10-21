package com.nikoladx.trailator.data.models
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val lastName: String = "",
    val imageUri: String? = null,
    val points: Long = 0,
    val rank: UserRank = UserRank.NOVICE,
    val achievedBadges: List<String> = emptyList(),
    val objectsAdded: Int = 0,
    val commentsPosted: Int = 0,
    val locationsVisited: Int = 0,
    val visitedObjectIds: List<String> = emptyList(),
    @ServerTimestamp
    val memberSince: Date? = null,
    @ServerTimestamp
    val lastActive: Date? = null,
    val location: GeoPoint? = null
) {
    fun calculateRank(): UserRank {
        return when {
            points >= 5000 -> UserRank.MASTER_EXPLORER
            points >= 2000 -> UserRank.EXPERT_HIKER
            points >= 1000 -> UserRank.ADVANCED_TREKKER
            points >= 500 -> UserRank.TRAIL_SEEKER
            points >= 100 -> UserRank.ENTHUSIAST
            else -> UserRank.NOVICE
        }
    }
}

enum class UserRank(val displayName: String) {
    NOVICE("Novice"),
    ENTHUSIAST("Enthusiast"),
    TRAIL_SEEKER("Trail Seeker"),
    ADVANCED_TREKKER("Advanced Trekker"),
    EXPERT_HIKER("Expert Hiker"),
    MASTER_EXPLORER("Master Explorer")
}

