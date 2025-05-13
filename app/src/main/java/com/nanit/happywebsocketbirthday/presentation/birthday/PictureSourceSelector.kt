package com.nanit.happywebsocketbirthday.presentation.birthday

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nanit.happywebsocketbirthday.R
import com.nanit.happywebsocketbirthday.presentation.utils.FileProviderUtils

/**
 * A Composable that encapsulates the logic for selecting an image from the gallery or camera,
 * including permission handling and launching appropriate intents.
 *
 * @param showDialog A boolean state that controls the visibility of the picture source dialog.
 * @param onDismissDialog Callback for when the dialog is dismissed.
 * @param onPictureSelected Callback for when a picture is successfully selected (from gallery or camera).
 */
@Composable
fun PictureSourceSelector(
    showDialog: Boolean,
    onDismissDialog: () -> Unit,
    onPictureSelected: (Uri) -> Unit
) {
    val context = LocalContext.current
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    // State to track if we have pending action to take a photo after permission
    var pendingPhotoCapture by remember { mutableStateOf(false) }

    // State to track if we should show a message guiding the user to settings
    var showSettingsGuidance by remember { mutableStateOf(false) }

    // Launcher for selecting a picture from the gallery
    val pickPictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                onPictureSelected(uri) // Pass the selected Uri back
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.picture_selection_cancelled), Toast.LENGTH_SHORT
                ).show()
            }
            onDismissDialog()
        }
    )

    // Launcher for taking a picture with the camera
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success: Boolean ->
            if (success) {
                // Picture was saved to the tempPhotoUri
                tempPhotoUri?.let { onPictureSelected(it) } // Pass the captured Uri back
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.taking_picture_failed_or_cancelled),
                    Toast.LENGTH_SHORT
                ).show()
            }
            tempPhotoUri = null // Clear temporary Uri after use
            pendingPhotoCapture = false // Reset the pending state
            onDismissDialog()
        }
    )

    // Permission launcher for camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, set pending state to trigger photo capture
                pendingPhotoCapture = true
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT
                ).show()
                pendingPhotoCapture = false // Ensure pending state is false if permission is denied
            }

            // Check if we should show the settings guidance
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.CAMERA
                )
            ) {
                showSettingsGuidance = true
            }
            // The PictureSourceDialog remains open, allowing the user to try again, see guidance, or cancel.
        }
    )

    // Logic to handle the "Take Photo" action, including permissions
    val handleTakePhotoClick: () -> Unit = {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted, proceed directly to photo capture
                pendingPhotoCapture = true
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity,
                Manifest.permission.CAMERA
            ) -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.camera_permission_is_needed_to_take_photos),
                    Toast.LENGTH_LONG
                ).show()
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }

            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // LaunchedEffect to launch the camera when pendingPhotoCapture becomes true
    LaunchedEffect(pendingPhotoCapture) {
        if (pendingPhotoCapture && ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // We have pending action and permission is granted, proceed to take photo
            val photoFile = FileProviderUtils.createTempImageFile(context)
            if (photoFile != null) {
                val photoUri = FileProviderUtils.getUriForFile(context, photoFile)
                if (photoUri != null) {
                    tempPhotoUri = photoUri // Store the Uri temporarily
                    takePictureLauncher.launch(photoUri) // Launch the camera with the Uri
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.error_generating_uri_for_picture),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("PictureSourceSelector", "getUriForFile returned null")
                    pendingPhotoCapture = false // Reset if Uri generation fails
                    onDismissDialog()
                }
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_creating_file_for_picture),
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("PictureSourceSelector", "createTempImageFile returned null")
                pendingPhotoCapture = false // Reset if file creation fails
                onDismissDialog()
            }
        }
    }

    // Show the picture source dialog if the showDialog state is true
    if (showDialog) {
        PictureSourceDialog(
            onDismiss = onDismissDialog,
            onChooseGallery = {
                pickPictureLauncher.launch("image/*")
                onDismissDialog()
            },
            onTakePhoto = {
                handleTakePhotoClick()
            }
        )
    }
    // Show settings guidance dialog if needed
    if (showSettingsGuidance) {
        AlertDialog(
            onDismissRequest = { showSettingsGuidance = false },
            title = { Text("Permission Required") },
            text = {
                Text("Camera permission is required to take photos. Please go to app settings to grant permission.")
            },
            confirmButton = {
                Button(onClick = {
                    showSettingsGuidance = false
                    onDismissDialog()
                    // Direct user to app settings
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("Go to Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsGuidance = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}