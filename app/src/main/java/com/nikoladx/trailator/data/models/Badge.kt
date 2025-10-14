package com.nikoladx.trailator.data.models

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val condition: BadgeCondition
)

sealed class BadgeCondition {
    data class ObjectsAdded(val count: Int) : BadgeCondition()
    data class LocationsVisited(val count: Int) : BadgeCondition()
    data class TotalPoints(val points: Int) : BadgeCondition()
    data class CommentsPosted(val count: Int) : BadgeCondition()
    data class HighRatings(val count: Int, val minRating: Double) : BadgeCondition()
}

object Badges {
    val FIRST_STEP = Badge(
        id = "first_step",
        name = "First Step",
        description = "Add your first trail object",
        iconUrl = "",
        condition = BadgeCondition.ObjectsAdded(1)
    )

    val EXPLORER = Badge(
        id = "explorer",
        name = "Explorer",
        description = "Add 10 trail objects",
        iconUrl = "",
        condition = BadgeCondition.ObjectsAdded(10)
    )

    val TRAILBLAZER = Badge(
        id = "trailblazer",
        name = "Trailblazer",
        description = "Add 50 trail objects",
        iconUrl = "",
        condition = BadgeCondition.ObjectsAdded(50)
    )

    val NAVIGATOR = Badge(
        id = "navigator",
        name = "Navigator",
        description = "Visit 25 locations",
        iconUrl = "",
        condition = BadgeCondition.LocationsVisited(25)
    )

    val COMMENTATOR = Badge(
        id = "commentator",
        name = "Commentator",
        description = "Post 50 comments",
        iconUrl = "",
        condition = BadgeCondition.CommentsPosted(50)
    )

    val CHAMPION = Badge(
        id = "champion",
        name = "Champion",
        description = "Reach 1000 points",
        iconUrl = "",
        condition = BadgeCondition.TotalPoints(1000)
    )

    val LEGEND = Badge(
        id = "legend",
        name = "Legend",
        description = "Reach 5000 points",
        iconUrl = "",
        condition = BadgeCondition.TotalPoints(5000)
    )

    fun getAll() = listOf(
        FIRST_STEP, EXPLORER, TRAILBLAZER,
        NAVIGATOR, COMMENTATOR, CHAMPION, LEGEND
    )
}