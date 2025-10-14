package com.nikoladx.trailator.ui.objects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nikoladx.trailator.data.models.TrailObject
import com.nikoladx.trailator.ui.components.CloudinaryImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectDetailsBottomSheet(
    trailObject: TrailObject,
    userId: String,
    onDismiss: () -> Unit,
    onRate: (Int) -> Unit,
    onComment: (String, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var commentText by remember { mutableStateOf("") }
    var selectedRating by remember { mutableIntStateOf(trailObject.ratings[userId] ?: 0) }
    val dateFormatter = remember { SimpleDateFormat("dd.MM.yyyy., HH:mm", Locale.getDefault()) }

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
                    Text(
                        text = trailObject.title,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssistChip(
                            onClick = {},
                            label = { Text(trailObject.type.name.replace("_", " ")) }
                        )

                        Text(
                            text = "by ${trailObject.authorName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (trailObject.photoUrls.isNotEmpty()) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(trailObject.photoUrls) { url ->
                            CloudinaryImage(
                                url = url,
                                contentDescription = "Trail object photo",
                                modifier = Modifier
                                    .width(250.dp)
                                    .height(150.dp),
                                width = 500,
                                height = 300,
                                quality = "auto:good"
                            )
                        }
                    }
                }
            }

            // Opis
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = trailObject.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Dodatne informacije
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
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Ocena
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
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = String.format("%.1f / 5.0", trailObject.getAverageRating()),
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }

                            Text(
                                text = "${trailObject.ratings.size} ratings",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )

                        Text(
                            text = "Your Rating",
                            style = MaterialTheme.typography.labelLarge
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

            // Komentari
            item {
                Text(
                    text = "Comments (${trailObject.comments.size})",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Dodaj komentar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Add a comment...") },
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                onComment(commentText, "User") // Username treba preuzeti iz profila
                                commentText = ""
                            }
                        },
                        enabled = commentText.isNotBlank()
                    ) {
                        Text("Post")
                    }
                }
            }

            // Lista komentara
            items(trailObject.comments) { comment ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = comment.userName,
                                style = MaterialTheme.typography.labelLarge
                            )

                            comment.timestamp?.let {
                                Text(
                                    text = dateFormatter.format(it),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = comment.text,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Dodatni spacing na dnu
            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}