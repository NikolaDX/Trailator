package com.nikoladx.trailator.ui.screens.home.profile.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nikoladx.trailator.R
import com.nikoladx.trailator.data.models.User

@Composable
fun ProfileBadges(user: User) {
    if (user.achievedBadges.isEmpty()) return

    Text(
        text = "Badges",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        user.achievedBadges.forEach { badgeName ->
            val badgeRes = when (badgeName) {
                "NOVICE" -> R.drawable.badge_novice
                "ENTHUSIAST" -> R.drawable.badge_enthusiast
                "TRAIL_SEEKER" -> R.drawable.badge_trail_seeker
                "ADVANCED_TREKKER" -> R.drawable.badge_advanced_trekker
                "EXPERT_HIKER" -> R.drawable.badge_expert_hiker
                "MASTER_EXPLORER" -> R.drawable.badge_master_explorer
                else -> R.drawable.badge_novice
            }
            Image(
                painter = painterResource(id = badgeRes),
                contentDescription = badgeName,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

