package com.nanit.happywebsocketbirthday.presentation.birthday

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun PictureSourceDialog(
    onDismiss: () -> Unit,
    onChooseGallery: () -> Unit,
    onTakePhoto: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Picture Source") },
        text = {
            Column {
                Button(
                    onClick = onChooseGallery,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text("Choose from Gallery")
                }
                Button(onClick = onTakePhoto) {
                    Text("Take Photo")
                }
            }
        },
        confirmButton = {},// No confirmation button needed
        dismissButton = { // Add a dismiss button
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    )
}
