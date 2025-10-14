package com.nikoladx.trailator.ui.objects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nikoladx.trailator.data.models.TrailDifficulty
import com.nikoladx.trailator.data.models.TrailObjectFilter
import com.nikoladx.trailator.data.models.TrailObjectType
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    currentFilter: TrailObjectFilter,
    onDismiss: () -> Unit,
    onApplyFilter: (TrailObjectFilter) -> Unit
) {
    var selectedTypes by remember { mutableStateOf(currentFilter.types ?: emptyList()) }
    var minRating by remember { mutableStateOf(currentFilter.minRating?.toString() ?: "") }
    var searchQuery by remember { mutableStateOf(currentFilter.searchQuery ?: "") }
    var selectedDifficulty by remember { mutableStateOf(currentFilter.difficulty) }
    var tags by remember { mutableStateOf(currentFilter.tags?.joinToString(", ") ?: "") }
    var authorId by remember { mutableStateOf(currentFilter.authorId ?: "") }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Filter Objects",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search") },
                    placeholder = { Text("Title or description...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Object Types", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TrailObjectType.entries.forEach { type ->
                        FilterChip(
                            selected = type in selectedTypes,
                            onClick = {
                                selectedTypes = if (type in selectedTypes) {
                                    selectedTypes - type
                                } else {
                                    selectedTypes + type
                                }
                            },
                            label = { Text(type.name.replace("_", " ")) }
                        )
                    }
                }

                OutlinedTextField(
                    value = minRating,
                    onValueChange = {
                        if (it.isEmpty() || it.toDoubleOrNull() != null) {
                            minRating = it
                        }
                    },
                    label = { Text("Minimum Rating (0-5)") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (TrailObjectType.TRAIL in selectedTypes || selectedTypes.isEmpty()) {
                    Text("Trail Difficulty", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FilterChip(
                            selected = selectedDifficulty == null,
                            onClick = { selectedDifficulty = null },
                            label = { Text("All") }
                        )
                        TrailDifficulty.entries.forEach { diff ->
                            FilterChip(
                                selected = selectedDifficulty == diff,
                                onClick = { selectedDifficulty = diff },
                                label = { Text(diff.name) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma separated)") },
                    placeholder = { Text("mountain, scenic") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = authorId,
                    onValueChange = { authorId = it },
                    label = { Text("Author ID (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            onApplyFilter(TrailObjectFilter())
                        }
                    ) {
                        Text("Clear All")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val filter = TrailObjectFilter(
                                    types = selectedTypes.takeIf { it.isNotEmpty() },
                                    minRating = minRating.toDoubleOrNull(),
                                    searchQuery = searchQuery.takeIf { it.isNotBlank() },
                                    difficulty = selectedDifficulty,
                                    tags = tags.split(",")
                                        .map { it.trim() }
                                        .filter { it.isNotEmpty() }
                                        .takeIf { it.isNotEmpty() },
                                    authorId = authorId.takeIf { it.isNotBlank() }
                                )
                                onApplyFilter(filter)
                            }
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // Jednostavna implementacija FlowRow-a
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}