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
    val objectsRated: Int = 0,
    val commentsPosted: Int = 0,
    val locationsVisited: Int = 0,
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

enum class UserRank(val displayName: String, val minPoints: Int) {
    NOVICE("Novice", 0),
    ENTHUSIAST("Enthusiast", 100),
    TRAIL_SEEKER("Trail Seeker", 500),
    ADVANCED_TREKKER("Advanced Trekker", 1000),
    EXPERT_HIKER("Expert Hiker", 2000),
    MASTER_EXPLORER("Master Explorer", 5000)
}

