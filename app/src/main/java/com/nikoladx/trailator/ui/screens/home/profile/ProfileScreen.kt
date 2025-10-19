package com.nikoladx.trailator.ui.screens.home.profile

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
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
import com.nikoladx.trailator.ui.screens.home.profile.components.EditFields
import com.nikoladx.trailator.ui.screens.home.profile.components.ProfileBadges
import com.nikoladx.trailator.ui.screens.home.profile.components.ProfileDetails
import com.nikoladx.trailator.ui.screens.home.profile.components.ProfileHeader
import com.nikoladx.trailator.ui.screens.home.profile.viewmodels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    loggedInUserId: String,
    targetUserId: String,
    onSetTopBarActions: (isEditing: Boolean, onSave: () -> Unit, onEdit: () -> Unit, canEdit: Boolean) -> Unit,
    onAccountDeleted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user
    val canEdit = loggedInUserId == targetUserId

    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(user.name) }
    var lastName by remember { mutableStateOf(user.lastName) }
    var localImageUri by remember { mutableStateOf<Uri?>(null) }

    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshUserProfile()
    }

    LaunchedEffect(isEditing, canEdit) {
        onSetTopBarActions(
            isEditing,
            {
                viewModel.saveProfile(name.trim(), lastName.trim(), localImageUri?.toString())
                isEditing = false
                localImageUri = null
            },
            { isEditing = true },
            canEdit
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account") },
            text =  {
                Text("Are you sure you want to delete your account? This will permanently delete all your data including trail objects, ratings, and comments. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAccount(onSuccess =  onAccountDeleted)
                        showDeleteAccountDialog = false
                    }
                ) {
                    Text("Delete Forever")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileHeader(
                        user = user,
                        isEditing = isEditing,
                        localImageUri = localImageUri,
                        onImageSelected = { uri -> localImageUri = uri }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (isEditing) {
                        EditFields(
                            name = name,
                            onNameChange = { name = it },
                            lastName = lastName,
                            onLastNameChange = { lastName = it }
                        )
                    } else {
                        ProfileDetails(user)
                        ProfileBadges(user)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        if (uiState.successMessage != null) {
            Snackbar(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                action = {
                    TextButton(onClick = viewModel::dismissSuccessMessage) {
                        Text("OK")
                    }
                }
            ) {
                Text(uiState.successMessage!!)
            }
        }

        if (uiState.error != null) {
            Snackbar(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                containerColor = MaterialTheme.colorScheme.error,
                action = {
                    TextButton(onClick = viewModel::dismissError) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(uiState.error!!)
            }
        }

        if (canEdit && !isEditing) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                TextButton(
                    onClick = { showDeleteAccountDialog = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Account")
                }
            }
        }
    }


}