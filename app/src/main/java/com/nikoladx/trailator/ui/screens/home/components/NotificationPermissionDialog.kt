package com.nikoladx.trailator.ui.screens.home.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun NotificationPermissionDialog(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dozvoli notifikacije") },
        text = {
            Text(
                "Ova aplikacija koristi notifikacije da te obavesti kada si blizu " +
                        "interesantnih lokacija. Želiš li da dozvoliš notifikacije?"
            )
        },
        confirmButton = {
            TextButton(onClick = onRequestPermission) {
                Text("Dozvoli")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ne sada")
            }
        }
    )
}