package com.nikoladx.trailator.ui.screens.home.maps.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.nikoladx.trailator.data.models.TrailDifficulty
import com.nikoladx.trailator.data.models.TrailObjectFilter
import com.nikoladx.trailator.data.models.TrailObjectType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    currentFilter: TrailObjectFilter,
    onDismiss: () -> Unit,
    onApplyFilter: (TrailObjectFilter) -> Unit
) {
    var selectedTypes by remember { mutableStateOf(currentFilter.types ?: emptyList()) }
    var searchQuery by remember { mutableStateOf(currentFilter.searchQuery ?: "") }
    var selectedDifficulty by remember { mutableStateOf(currentFilter.difficulty) }
    var tags by remember { mutableStateOf(currentFilter.tags?.joinToString(", ") ?: "") }
    var authorId by remember { mutableStateOf(currentFilter.authorId ?: "") }
    var minRating by remember { mutableDoubleStateOf(currentFilter.minRating ?: 0.0) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )

                    Text("Object Types", style = MaterialTheme.typography.labelLarge)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TrailObjectType.entries.forEach { type ->
                            val isSelected = type in selectedTypes
                            FilterChip(
                                selected = type in selectedTypes,
                                onClick = {
                                    selectedTypes = if (type in selectedTypes) {
                                        selectedTypes - type
                                    } else {
                                        selectedTypes + type
                                    }
                                },
                                label = { Text(type.name.replace("_", " ")) },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            Icons.Filled.Done,
                                            null,
                                            modifier = Modifier.size(
                                                FilterChipDefaults.IconSize
                                            )
                                        )
                                    }
                                } else {
                                    null
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    Text("Minimum Rating", style = MaterialTheme.typography.labelLarge)

                    StarRatingSelector(
                        currentRating = minRating.toInt(),
                        onRatingChange = { newRating ->
                            minRating = newRating.toDouble()
                        }
                    )

                    if (TrailObjectType.TRAIL in selectedTypes || selectedTypes.isEmpty()) {
                        Text("Trail Difficulty", style = MaterialTheme.typography.labelLarge)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedDifficulty == null,
                                onClick = { selectedDifficulty = null },
                                label = { Text("All") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                            TrailDifficulty.entries.forEach { diff ->
                                val isSelected = selectedDifficulty == diff
                                FilterChip(
                                    selected = selectedDifficulty == diff,
                                    onClick = { selectedDifficulty = diff },
                                    label = { Text(diff.name) },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                Icons.Filled.Done,
                                                null,
                                                modifier = Modifier.size(
                                                    FilterChipDefaults.IconSize
                                                )
                                            )
                                        }
                                    } else {
                                        null
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
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
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                onApplyFilter(TrailObjectFilter())
                            }
                        ) {
                            Text("Clear All")
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = onDismiss) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    val filter = TrailObjectFilter(
                                        types = selectedTypes.takeIf { it.isNotEmpty() },
                                        minRating = minRating,
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
}