package com.nikoladx.trailator.ui.screens.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nikoladx.trailator.data.models.User
import java.text.SimpleDateFormat

@Composable
fun ProfileDetails(
    user: User
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DetailRow("Email", user.email)
        DetailRow("Points", user.points.toString())
        DetailRow("Objects Added", user.objectsAdded.toString())
        DetailRow("Comments Posted", user.commentsPosted.toString())
        DetailRow("Locations Visited", user.locationsVisited.toString())
        user.memberSince?.let {
            DetailRow("Member Since", SimpleDateFormat.getDateInstance().format(it))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Achieved Badges (${user.achievedBadges.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = if (user.achievedBadges.isEmpty()) "No badges yet." else user.achievedBadges.joinToString(),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}