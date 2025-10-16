package com.nikoladx.trailator.ui.screens.home

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nikoladx.trailator.ui.screens.home.components.EditFields
import com.nikoladx.trailator.ui.screens.home.components.ProfileDetails
import com.nikoladx.trailator.ui.screens.home.components.ProfileHeader
import com.nikoladx.trailator.ui.screens.home.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    currentUserId: String,
    onSetTopBarActions: @Composable (isEditing: Boolean, onSave: () -> Unit) -> Unit,
    setOnEditAction: (() -> Unit) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user

    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(user.name) }
    var lastName by remember { mutableStateOf(user.lastName) }
    var localImageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        setOnEditAction {
            isEditing = true
        }
    }

    LaunchedEffect(user) {
        name = user.name
        lastName = user.lastName
    }

    val onSaveAction: () -> Unit = {
        viewModel.saveProfile(
            name.trim(),
            lastName.trim(),
            localImageUri?.toString()
        )
        isEditing = false
        localImageUri = null
    }

    onSetTopBarActions(isEditing, onSaveAction)

    if (uiState.isLoading) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileHeader(user, isEditing, localImageUri) { uri -> localImageUri = uri }

            Spacer(modifier = Modifier.height(24.dp))

            if (isEditing) {
                EditFields(name, { name = it }, lastName, { lastName = it })
            } else {
                ProfileDetails(user)
            }
        }
    }

    if (uiState.successMessage != null) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = { TextButton(onClick = viewModel::dismissSuccessMessage) { Text("OK") } }
        ) {
            Text(uiState.successMessage!!)
        }
    }
    if (uiState.error != null) {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            containerColor = MaterialTheme.colorScheme.error,
            action = { TextButton(onClick = viewModel::dismissError) { Text("Dismiss") } }
        ) {
            Text(uiState.error!!)
        }
    }
}