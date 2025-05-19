package com.nanit.happywebsocketbirthday.presentation.birthday

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.rememberAsyncImagePainter
import com.nanit.happywebsocketbirthday.R
import com.nanit.happywebsocketbirthday.ui.theme.AppTheme
import kotlin.math.sqrt

/**
 * Composable function to display the birthday screen for a baby.
 *
 * This function displays the baby's age, name, picture, and theme-specific background and icons.
 * It handles the loading and error states of the UI.
 *
 * @param uiState The [BirthdayScreenState] representing the current state of the UI,
 * including baby information, age display information, loading status, and error messages.
 * @param onCameraIconClick Callback function invoked when the camera icon is clicked.
 */
@Composable
fun BirthdayScreen(
    uiState: BirthdayScreenState,
    onCameraIconClick: () -> Unit // Callback for the camera icon click
) {
    if (uiState.isLoading) {
        // Display a loading indicator if data is not ready
        ShowLoading(uiState)
        return // Stop rendering if data is not ready
    }

    val currentTheme = AppTheme.fromThemeName(uiState.theme)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(), // Make the column fill the Box
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
                AgeNameDisplay(uiState.name, uiState.ageDisplayInfo)
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
                val cameraIconSize = 42.dp // Define camera icon size as Dp

                // State to hold the measured size of the image in Dp
                var imageWidthDp by remember { mutableStateOf(0.dp) }
                val density = LocalDensity.current

                Image(
                    // Use rememberAsyncImagePainter to load the image from Uri or Drawable ID
                    painter = rememberAsyncImagePainter(
                        model = uiState.pictureUri
                            ?: currentTheme.faceIconDrawableId, // Use chosen Uri if available, otherwise use the theme face drawable ID
                    ),
                    contentDescription = stringResource(R.string.baby_image),
                    modifier = Modifier
                        .aspectRatio(1f) // 1f means width:height ratio is 1:1 (a square)
                        .onSizeChanged { intSize ->
                            // Convert the IntSize (pixels) to Dp
                            imageWidthDp = with(density) { intSize.width.toDp() }
                        }
                        .clip(CircleShape) // Clip to a circle shape
                        .border( // Add the border modifier after clipping
                            width = 8.dp, // Specify the border width
                            color = currentTheme.darkColor, // Specify the border color
                            shape = CircleShape // Apply the border in a circular shape
                        ),
                    contentScale = ContentScale.Crop // Crop the image to fill the circle
                )

                // The diameter of the circle
                val circleDiameter = (imageWidthDp.value)
                if (circleDiameter > 0) { // Ensure image has been measured
                    val offset = (circleDiameter * sqrt(2.0)) / 4 // Calculate offset based on Dp value

                    IconButton(
                        onClick = { onCameraIconClick() },
                        modifier = Modifier
                            .size(cameraIconSize)
                            .offset(x = offset.dp, y = -offset.dp)
                    ) {
                        Image(
                            painter = painterResource(id = currentTheme.cameraIconDrawableId),
                            contentDescription = stringResource(R.string.choose_or_take_photo_icon),
                        )
                    }
                }
            }
        }
        // The background image
        Image(
            painter = painterResource(id = currentTheme.backgroundDrawableId),
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
    name: String,
    dateOfBirth: Long,
    theme: String,
    viewModel: BirthdayScreenViewModel = hiltViewModel()
) {

    // Use LaunchedEffect to call initialize when the ViewModel is available
    LaunchedEffect(viewModel) {
        viewModel.initialize(name, dateOfBirth, theme)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // State to control the visibility of the picture source dialog
    var showPictureSourceDialog by remember { mutableStateOf(false) }

    // Collect the SharedFlow from the ViewModel to trigger the dialog
    LaunchedEffect(viewModel.showPictureSourceDialog) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.showPictureSourceDialog.collect {
                showPictureSourceDialog = true // Set state to true to show the dialog
            }
        }
    }

    // Call the state-driven Composable, passing the current state and callbacks
    BirthdayScreen(
        uiState = uiState,
        onCameraIconClick = { viewModel.onCameraIconClick() } // Trigger the ViewModel function
    )

    // Use PictureSourceSelector to handle picture source selection
    PictureSourceSelector(
        showDialog = showPictureSourceDialog,
        onDismissDialog = { showPictureSourceDialog = false },
        onPictureSelected = { uri -> viewModel.onPictureSelected(uri) } // Pass the selected Uri to ViewModel
    )
}

@Preview(showBackground = true)
@Composable
fun BirthdayScreenPreview() {
    val sampleUiState = BirthdayScreenState(
        name = "Christiano Ronaldo",
        theme = "pelican",
        ageDisplayInfo = AgeDisplayInfo(R.plurals.months_old, 10)
    )

    MaterialTheme {
        Surface {
            BirthdayScreen(uiState = sampleUiState, onCameraIconClick = {})
        }
    }
}

@Composable
private fun ShowLoading(uiState: BirthdayScreenState) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator() // Show loading
        }
    }
}
