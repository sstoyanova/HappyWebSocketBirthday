package com.nanit.happywebsocketbirthday.presentation.birthday

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.rememberAsyncImagePainter
import com.nanit.happywebsocketbirthday.R
import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
import com.nanit.happywebsocketbirthday.presentation.utils.FileProviderUtils.createTempImageFile
import com.nanit.happywebsocketbirthday.presentation.utils.FileProviderUtils.getUriForFile
import com.nanit.happywebsocketbirthday.ui.theme.AppTheme
import com.nanit.happywebsocketbirthday.ui.theme.DarkBlueTextColor
import kotlin.math.sqrt

@Composable
fun BirthdayScreen(
    uiState: BirthdayScreenState,
    onCameraIconClick: () -> Unit // Callback for the camera icon click
) {
    // Ensure babyInfo and ageDisplayInfo are not null before accessing their properties
    // You might want to show a loading or error state if they are null
    if (uiState.babyInfo == null || uiState.ageDisplayInfo == null) {
        // Display a loading indicator or an error message if data is not ready
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator() // Show loading
            } else if (uiState.errorMessage != null) {
                Text(uiState.errorMessage) // Show error
            } else {
                Text(stringResource(R.string.loading_baby_info)) // Default loading text
            }
        }
        return // Stop rendering if data is not ready
    }

    val currentTheme = AppTheme.fromThemeName(uiState.babyInfo.theme)

    val backgroundDrawableId = currentTheme.backgroundDrawableId
    val faceDrawableId = currentTheme.faceIconDrawableId
    val cameraIconDrawableId = currentTheme.cameraIconDrawableId
    val backgroundColor = currentTheme.backgroundColor
    val darkColor = currentTheme.darkColor

    val babyNameLabel = stringResource(R.string.today_baby_name_is_label, uiState.babyInfo.name)
    val ageLabelText = pluralStringResource(
        id = uiState.ageDisplayInfo.ageLabelPluralResId, // Use the plural resource ID
        uiState.ageDisplayInfo.age // Directly use the age from AgeDisplayInfo as the quantity
    )

    val bentonSansFamily = FontFamily(
        Font(R.font.benton_sans_regular, FontWeight.Normal),
        Font(R.font.benton_sans_bold, FontWeight.Bold)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(), // Make the column fill the Box
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,

            ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(start = 50.dp, end = 50.dp)
                    .weight(1f)
            ) {
                Text(
                    babyNameLabel.uppercase(),
                    color = DarkBlueTextColor,
                    modifier = Modifier
                        .padding(top = 20.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 21.sp,
                    fontFamily = bentonSansFamily,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .padding(16.dp)
                ) {

                    Image(
                        painter = painterResource(id = R.drawable.left_swirls),
                        contentDescription = stringResource(R.string.decoration),
                        modifier = Modifier
                            .width(50.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Image(
                        painter = painterResource(id = uiState.ageDisplayInfo.numberIconDrawableId),
                        contentDescription = stringResource(R.string.age_icon),
                        modifier = Modifier
                            .height(88.dp)
                            .padding(start = 22.dp, end = 22.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.right_swirls),
                        contentDescription = stringResource(R.string.decoration),
                        modifier = Modifier
                            .width(50.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
                Text(
                    ageLabelText,
                    fontSize = 18.sp,
                    color = DarkBlueTextColor,
                    fontFamily = bentonSansFamily,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(
                        top = 15.dp,
                        start = 50.dp,
                        end = 50.dp,
                        bottom = 135.dp
                    )
                    .align(Alignment.CenterHorizontally)
            ) {
                val circleSize = 200
                val cameraIconSize = 56
                // Display the baby picture - use rememberAsyncImagePainter
                Image(
                    // Use rememberAsyncImagePainter to load the image from Uri or Drawable ID
                    painter = rememberAsyncImagePainter(
                        model = uiState.babyPictureUri
                            ?: faceDrawableId, // Use chosen Uri if available, otherwise use the theme face drawable ID
                    ),
                    contentDescription = stringResource(R.string.baby_image),
                    modifier = Modifier
                        .size(circleSize.dp) // Apply the defined size
                        .clip(CircleShape) // Clip to a circle shape
                        .border( // Add the border modifier after clipping
                            width = 6.dp, // Specify the border width (adjust as needed)
                            color = darkColor, // Specify the border color (adjust as needed)
                            shape = CircleShape // Apply the border in a circular shape
                        ),
                    contentScale = ContentScale.Crop // Crop the image to fill the circle
                )

                val offset = (circleSize * sqrt(2.0)) / 4 // Calculate offset based on Dp value
                IconButton(
                    onClick = { onCameraIconClick() },
                    modifier = Modifier
                        .size(cameraIconSize.dp) // Apply the defined size
                        .offset(offset.dp, -offset.dp) // Apply the calculated offset as Dp
                ) {
                    // Use painterResource for the camera icon as it's a drawable
                    Image(
                        painter = painterResource(id = cameraIconDrawableId),
                        contentDescription = stringResource(R.string.choose_or_take_photo_icon),
                    )
                }
            }
        }
        // The background image
        Image(
            painter = painterResource(id = backgroundDrawableId),
            contentDescription = stringResource(R.string.background_image),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.FillWidth
        )
        //Nanit logo
        Image(
            painter = painterResource(id = R.drawable.nanit),
            contentDescription = stringResource(R.string.nanit_logo),
            modifier = Modifier
                .padding(
                    bottom = 100.dp
                )
                .width(58.dp)
                .height(20.dp)
                .align(Alignment.BottomCenter)
        )
    }
}

// Original BirthdayScreen that uses HiltViewModel
@Composable
fun BirthdayScreen(
    viewModel: BirthdayScreenViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // 2. State to control the visibility of the picture source dialog
    var showPictureSourceDialog by remember { mutableStateOf(false) }

    // 3. State to temporarily hold the Uri for the picture taken by the camera
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    // Launcher for selecting a picture from the gallery
    val pickPictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            // Handle the selected URI from the gallery
            if (uri != null) {
                viewModel.onPictureSelected(uri) // Update ViewModel state with the selected Uri
            } else {
                // Handle the case where the user cancelled picture selection
                Toast.makeText(
                    context,
                    context.getString(R.string.picture_selection_cancelled), Toast.LENGTH_SHORT
                ).show()
            }
        }
    )

    // Launcher for taking a picture with the camera
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success: Boolean ->
            // Handle the result (success or failure) from the camera
            if (success) {
                // Picture was saved to the tempPhotoUri you provided
                viewModel.onPictureSelected(tempPhotoUri) // Update ViewModel state with the temporary Uri
            } else {
                // Handle the case where taking a picture failed or was cancelled
                Toast.makeText(
                    context,
                    context.getString(R.string.taking_picture_failed_or_cancelled),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            tempPhotoUri = null // Clear temporary Uri after use, regardless of success
        }
    )

    // Permission launcher for camera (needed on Android 6.0+)
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            if (isGranted) {
                // Permission granted, now proceed to create a Uri and launch the camera
                val photoFile = createTempImageFile(context)
                // IMPORTANT: Check if photoFile was created successfully
                if (photoFile != null) {
                    val photoUri = getUriForFile(context, photoFile) // Use the utility object

                    // IMPORTANT: Check if photoUri was generated successfully
                    if (photoUri != null) {
                        tempPhotoUri = photoUri // Store the Uri temporarily before launching
                        takePictureLauncher.launch(photoUri) // Launch the camera with the Uri
                    } else {
                        // Handle the case where getUriForFile failed
                        Toast.makeText(
                            context,
                            context.getString(R.string.error_generating_uri_for_picture),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(
                            "BirthdayScreen",
                            "getUriForFile returned null after permission granted"
                        ) // Log the error
                    }
                } else {
                    // Handle the case where createTempImageFile failed
                    Toast.makeText(
                        context,
                        context.getString(R.string.error_creating_file_for_picture),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    Log.e(
                        "BirthdayScreen",
                        "createTempImageFile returned null after permission granted"
                    ) // Log the error
                }
            } else {
                // Permission denied
                Toast.makeText(
                    context,
                    context.getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT
                ).show()
            }
        }
    )

    // --- Observe ViewModel Events (e.g., to show dialog) ---

    // Collect the SharedFlow from the ViewModel to trigger the dialog
    LaunchedEffect(viewModel.showPictureSourceDialog) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.showPictureSourceDialog.collect {
                showPictureSourceDialog = true // Set state to true to show the dialog
            }
        }
    }

    // --- Helper Function to Handle Camera Click (including permissions) ---
    // Function to handle the "Take Photo" click from the dialog
    fun handleTakePhotoClick() {
        // Check for camera permission
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted, proceed to create Uri and launch camera
                val photoFile = createTempImageFile(context)
                if (photoFile != null) {
                    val photoUri = getUriForFile(context, photoFile)
                    if (photoUri != null) {
                        tempPhotoUri = photoUri // Store the Uri temporarily
                        takePictureLauncher.launch(photoUri) // Launch the camera with the Uri
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.error_creating_file_for_picture),
                            Toast.LENGTH_SHORT
                        ).show()
                        // Handle the case where getUriForFile failed
                        Toast.makeText(
                            context,
                            context.getString(R.string.error_generating_uri_for_picture),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(
                            "BirthdayScreen",
                            "getUriForFile returned null"
                        ) // Log the error
                    }
                } else {
                    // Handle the case where createTempImageFile failed
                    Toast.makeText(
                        context,
                        context.getString(R.string.error_creating_file_for_picture),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    Log.e(
                        "BirthdayScreen",
                        "createTempImageFile returned null"
                    ) // Log the error
                }
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                context as Activity, // Requires casting context to Activity
                Manifest.permission.CAMERA
            ) -> {
                // Explain why the permission is needed if the user previously denied it
                Toast.makeText(
                    context,
                    context.getString(R.string.camera_permission_is_needed_to_take_photos),
                    Toast.LENGTH_LONG
                ).show()
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA) // Request again
            }

            else -> {
                // Directly request the permission
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // --- Render the UI ---

    // Call the state-driven Composable, passing the current state and callbacks
    BirthdayScreen(
        uiState = uiState,
        onCameraIconClick = { viewModel.onCameraIconClick() } // Trigger the ViewModel function
    )

    // --- Show Dialog if State is True ---

    // Show the picture source dialog if showPictureSourceDialog is true
    if (showPictureSourceDialog) {
        PictureSourceDialog(
            onDismiss = { showPictureSourceDialog = false }, // Dismiss the dialog
            onChooseGallery = {
                pickPictureLauncher.launch("image/*") // Launch gallery picker
                showPictureSourceDialog = false // Dismiss dialog after launching
            },
            onTakePhoto = {
                handleTakePhotoClick() // Handle camera click with permission check
                showPictureSourceDialog = false // Dismiss dialog after launching
            }
        )
    }
}

// --- Picture Source Dialog Composable ---

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
                Button(onClick = onChooseGallery) {
                    Text("Choose from Gallery")
                }
                Spacer(modifier = Modifier.height(8.dp)) // Import Spacer
                Button(onClick = onTakePhoto) {
                    Text("Take Photo")
                }
            }
        },
        confirmButton = {} // No confirmation button needed
    )
}

@Preview(showBackground = true)
@Composable
fun BirthdayScreenPreview() {
    val sampleBabyInfo = BabyInfo(
        name = "Christiano Ronaldo",
        dateOfBirth = 1640995200000, // Example DOB (Jan 1, 2022)
        theme = "pelican"
    )

    val sampleUiState = BirthdayScreenState(
        babyInfo = sampleBabyInfo,
        isLoading = false,
        errorMessage = null,
        ageDisplayInfo = AgeDisplayInfo(R.drawable.icon_10, R.plurals.months_old, 10)
    )

    MaterialTheme {
        Surface {
            BirthdayScreen(uiState = sampleUiState, onCameraIconClick = {})
        }
    }
}


