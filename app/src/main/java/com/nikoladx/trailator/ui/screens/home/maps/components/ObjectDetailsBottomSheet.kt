package com.nikoladx.trailator.ui.screens.home.maps.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nikoladx.trailator.data.models.TrailObject
import com.nikoladx.trailator.ui.screens.home.maps.viewmodels.MapViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectDetailsBottomSheet(
    trailObject: TrailObject,
    userId: String,
    onDismiss: () -> Unit,
    onRate: (Int) -> Unit,
    onComment: (String) -> Unit,
    onDelete: (String) -> Unit,
    viewModel: MapViewModel,
    onNavigateToProfile: (userId: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var commentText by remember { mutableStateOf("") }
    var selectedRating by remember { mutableIntStateOf(trailObject.ratings[userId] ?: 0) }
    var showFullScreenImage by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableIntStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val userImageUrl by viewModel
        .getUserImageUriFlow(trailObject.authorId)
        .collectAsState(initial = null)
    val userName by viewModel
        .getUserName(trailObject.authorId)
        .collectAsState(initial = null)

    val isAuthor = userId == trailObject.authorId

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Object") },
            text = { Text("Are you sure you want to delete this trail object? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(trailObject.id)
                        showDeleteDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showFullScreenImage && trailObject.photoUrls.isNotEmpty()) {
        FullScreenImageViewer(
            imageUrls = trailObject.photoUrls,
            initialPage = selectedImageIndex,
            onDismiss = { showFullScreenImage = false }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = trailObject.title,
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.weight(1f)
                        )

                        if (isAuthor) {
                            IconButton(
                                onClick = { showDeleteDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete object",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onNavigateToProfile(trailObject.authorId)
                                onDismiss()
                            }
                            .padding(vertical = 4.dp, horizontal = 0.dp)
                    ) {
                        AsyncImage(
                            model = userImageUrl,
                            contentDescription = "${userName}'s profile picture",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = userName ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AssistChip(
                        onClick = {},
                        label = { Text(trailObject.type.name.replace("_", " ")) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            labelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            if (trailObject.photoUrls.isNotEmpty()) {
                item {
                    PhotoCarousel(
                        photoUrls = trailObject.photoUrls,
                        onPhotoClick = { index ->
                            selectedImageIndex = index
                            showFullScreenImage = true
                        }
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = trailObject.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    trailObject.difficulty?.let {
                        InfoRow("Difficulty", it.name)
                    }

                    trailObject.waterQuality?.let {
                        InfoRow("Water Quality", it.name.replace("_", " "))
                    }

                    trailObject.capacity?.let {
                        InfoRow("Capacity", "$it people")
                    }

                    trailObject.elevation?.let {
                        InfoRow("Elevation", "${it}m")
                    }

                    if (trailObject.tags.isNotEmpty()) {
                        Text(
                            text = "Tags: ${trailObject.tags.joinToString(", ")}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Average Rating",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f / 5.0", trailObject.getAverageRating()),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = "${trailObject.ratings.size} ratings",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )

                        Text(
                            text = "Your Rating",
                            style = MaterialTheme.typography.titleLarge
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = {
                                        selectedRating = star
                                        onRate(star)
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (star <= selectedRating) {
                                            Icons.Filled.Star
                                        } else {
                                            Icons.Outlined.StarOutline
                                        },
                                        contentDescription = "$star stars",
                                        tint = if (star <= selectedRating) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Comments (${trailObject.comments.size})",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Add a comment as $userName...") },
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                onComment(commentText)
                                commentText = ""
                            }
                        },
                        enabled = commentText.isNotBlank()
                    ) {
                        Text("Post")
                    }
                }
            }

            items(trailObject.comments) { comment ->
                CommentItem(
                    modifier = Modifier.clickable {
                        onNavigateToProfile(comment.userId)
                        onDismiss()
                    },
                    comment = comment,
                    viewModel = viewModel
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}