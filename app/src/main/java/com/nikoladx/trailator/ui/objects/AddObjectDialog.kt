package com.nikoladx.trailator.ui.objects

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.google.firebase.firestore.GeoPoint
import com.nikoladx.trailator.data.models.*
import com.nikoladx.trailator.data.repositories.TrailObjectRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddObjectDialog(
    currentLocation: GeoPoint,
    userId: String,
    onDismiss: () -> Unit,
    onObjectAdded: () -> Unit,
    trailObjectRepository: TrailObjectRepository
) {
    val context = LocalContext.current
    val repository = remember { trailObjectRepository }
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TrailObjectType.TRAIL) }
    var selectedPhotos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var tags by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var error by remember { mutableStateOf<String?>(null) }
    var difficulty by remember { mutableStateOf<TrailDifficulty?>(null) }
    var waterQuality by remember { mutableStateOf<WaterQuality?>(null) }
    var capacity by remember { mutableStateOf("") }
    var elevation by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedPhotos = uris.take(5)
    }

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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add New Trail Object",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                Text("Object Type", style = MaterialTheme.typography.labelLarge)
                TrailObjectType.entries.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = type }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { selectedType = type }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(type.name.replace("_", " "))
                    }
                }

                when (selectedType) {
                    TrailObjectType.TRAIL -> {
                        Text("Difficulty", style = MaterialTheme.typography.labelLarge)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TrailDifficulty.entries.forEach { diff ->
                                FilterChip(
                                    selected = difficulty == diff,
                                    onClick = { difficulty = diff },
                                    label = { Text(diff.name) }
                                )
                            }
                        }
                    }
                    TrailObjectType.WATER_SOURCE -> {
                        Text("Water Quality", style = MaterialTheme.typography.labelLarge)
                        WaterQuality.entries.forEach { quality ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { waterQuality = quality }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = waterQuality == quality,
                                    onClick = { waterQuality = quality }
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(quality.name.replace("_", " "))
                            }
                        }
                    }
                    TrailObjectType.SHELTER -> {
                        OutlinedTextField(
                            value = capacity,
                            onValueChange = { capacity = it.filter { c -> c.isDigit() } },
                            label = { Text("Capacity (people)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    TrailObjectType.VIEWPOINT -> {
                        OutlinedTextField(
                            value = elevation,
                            onValueChange = { elevation = it },
                            label = { Text("Elevation (meters)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    else -> {}
                }

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("mountain, scenic, family-friendly") }
                )

                Text("Photos (max 5)", style = MaterialTheme.typography.labelLarge)

                if (selectedPhotos.isEmpty()) {
                    Button(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Photos")
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedPhotos) { uri ->
                            Box {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = {
                                        selectedPhotos = selectedPhotos - uri
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        if (selectedPhotos.size < 5) {
                            item {
                                IconButton(
                                    onClick = { photoPickerLauncher.launch("image/*") },
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Icon(Icons.Default.Add, "Add more")
                                }
                            }
                        }
                    }
                }

                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (isLoading && uploadProgress > 0f) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Uploading photos... ${(uploadProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                        LinearProgressIndicator(
                            progress = { uploadProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = ProgressIndicatorDefaults.linearColor,
                            trackColor = ProgressIndicatorDefaults.linearTrackColor,
                            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                if (title.isBlank() || description.isBlank()) {
                                    error = "Title and description are required"
                                    return@launch
                                }

                                isLoading = true
                                error = null

                                val result = repository.addTrailObject(
                                    userId = userId,
                                    type = selectedType,
                                    title = title,
                                    description = description,
                                    location = currentLocation,
                                    photoUris = selectedPhotos.map { it.toString() },
                                    difficulty = difficulty,
                                    waterQuality = waterQuality,
                                    capacity = capacity.toIntOrNull(),
                                    elevation = elevation.toDoubleOrNull(),
                                    tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                )

                                isLoading = false

                                result.onSuccess {
                                    onObjectAdded()
                                }.onFailure { e ->
                                    error = e.message ?: "Failed to add object"
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Add Object")
                        }
                    }
                }
            }
        }
    }
}