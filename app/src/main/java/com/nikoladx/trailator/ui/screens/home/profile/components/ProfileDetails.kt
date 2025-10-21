package com.nikoladx.trailator.ui.screens.home.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nikoladx.trailator.data.models.TrailObject
import com.nikoladx.trailator.data.models.User
import com.nikoladx.trailator.ui.screens.home.feed.components.TrailObjectCard
import com.nikoladx.trailator.ui.screens.home.maps.components.ObjectDetailsBottomSheet
import com.nikoladx.trailator.ui.screens.home.maps.viewmodels.MapViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileDetails(
    user: User,
    visitedTrails: List<TrailObject>,
    mapViewModel: MapViewModel,
    onNavigateToProfile: (String) -> Unit
) {
    var selectedTrailObject by remember { mutableStateOf<TrailObject?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    if (showBottomSheet && selectedTrailObject != null) {
        ObjectDetailsBottomSheet(
            trailObject = selectedTrailObject!!,
            userId = user.uid,
            onDismiss = {
                showBottomSheet = false
                selectedTrailObject = null
            },
            onRate = { rating ->
                mapViewModel.addRating(selectedTrailObject!!.id, user.uid, rating)
            },
            onComment = { comment ->
                coroutineScope.launch {
                    mapViewModel.addComment(selectedTrailObject!!.id, user.uid, comment)
                }
            },
            onDelete = { objectId ->
                coroutineScope.launch {
                    mapViewModel.deleteTrailObject(objectId, user.uid)
                    showBottomSheet = false
                }
            },
            viewModel = mapViewModel,
            onNavigateToProfile = onNavigateToProfile
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Activity Stats",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                DetailRow("Points", user.points.toString())
                DetailRow("Objects Added", user.objectsAdded.toString())
                DetailRow("Comments Posted", user.commentsPosted.toString())
                DetailRow("Locations Visited", user.locationsVisited.toString())
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Achieved Badges (${user.achievedBadges.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (user.achievedBadges.isEmpty()) {
                    Text("No badges yet.")
                } else {
                    ProfileBadges(user)
                }
            }
        }

        if (visitedTrails.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Visited Locations (${visitedTrails.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    visitedTrails.forEach { trail ->
                        TrailObjectCard(
                            trailObject = trail,
                            onClick = {
                                selectedTrailObject = trail
                                showBottomSheet = true
                            }
                        )
                    }
                }
            }
        }
    }
}