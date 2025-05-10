package com.nanit.happywebsocketbirthday.presentation.birthday

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nanit.happywebsocketbirthday.R
import com.nanit.happywebsocketbirthday.ui.theme.DarkBlueTextColor
import com.nanit.happywebsocketbirthday.ui.theme.ElephantGray
import com.nanit.happywebsocketbirthday.ui.theme.FoxOrange
import com.nanit.happywebsocketbirthday.ui.theme.PelicanBlue
import kotlin.math.sqrt

// Modify your BirthdayScreen to accept the UI state for easier previewing
@Composable
fun BirthdayScreen(uiState: BirthdayScreenState) {

// Determine the background drawable based on the theme
    val backgroundDrawableId = when (uiState.babyInfo.theme) {
        "pelican" -> R.drawable.bg_android_pelican // Use your PNG drawable resource names
        "fox" -> R.drawable.android_bg_fox
        "elephant" -> R.drawable.bg_android_elephant
        else -> R.drawable.bg_android_pelican // Default background if theme is unknown or null
    }

// Determine the face icon based on the theme
    val faceDrawableId = when (uiState.babyInfo.theme) {
        "pelican" -> R.drawable.blue_face // Use your PNG drawable resource names
        "fox" -> R.drawable.yellow_face
        "elephant" -> R.drawable.green_face
        else -> R.drawable.blue_face // Default face if theme is unknown or null
    }

    // Determine the face icon based on the theme
    val cameraIconDrawableId = when (uiState.babyInfo.theme) {
        "pelican" -> R.drawable.icon_camera_blue // Use your PNG drawable resource names
        "fox" -> R.drawable.icon_camera_yellow
        "elephant" -> R.drawable.icon_camera_teal
        else -> R.drawable.icon_camera_blue // Default icon if theme is unknown or null
    }

// Determine the background color based on the theme
    val backgroundColor = when (uiState.babyInfo.theme) {
        "pelican" -> PelicanBlue // Use your defined theme colors
        "fox" -> FoxOrange
        "elephant" -> ElephantGray
        else -> PelicanBlue // Default background color if theme is unknown or null
    }

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
                val babyNameLabel = "Today ${uiState.babyInfo.name} is"
                Text(
                    babyNameLabel.uppercase(),
                    color = DarkBlueTextColor,
                    modifier = Modifier
                        .padding(top = 20.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 21.sp,
                    fontFamily = bentonSansFamily, // Apply the custom font family
                    fontWeight = FontWeight.Bold // Specify the weight from the family
                )

                Row(
                    modifier = Modifier
                        .padding(16.dp)
                ) {

                    Image(
                        painter = painterResource(id = R.drawable.left_swirls),
                        contentDescription = "Nanit logo",
                        modifier = Modifier
                            .width(50.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Image(
                        painter = painterResource(id = uiState.ageDisplayInfo.numberIconDrawableId),
                        contentDescription = "Nanit logo",
                        modifier = Modifier
                            .height(88.dp)
                            .padding(start = 22.dp, end = 22.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.right_swirls),
                        contentDescription = "Nanit logo",
                        modifier = Modifier
                            .width(50.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
                Text(
                    uiState.ageDisplayInfo.ageLabelText,
                    fontSize = 18.sp,
                    color = DarkBlueTextColor
                )
            }
            Box(
                contentAlignment = Alignment.Center, // Center the content within the Box
                modifier = Modifier
                    .padding(
                        top = 15.dp,
                        start = 50.dp,
                        end = 50.dp,
                        bottom = 135.dp
                    )
                    .align(Alignment.CenterHorizontally) // Size of the main container
            ) {
                val circleSize = 200
                val cameraIconSize = 36
                Image(
                    painter = painterResource(id = faceDrawableId),
                    contentDescription = "Nanit logo",
                    modifier = Modifier
                        .size(circleSize.dp)
                )

                val offset = (circleSize * sqrt(2.0)) / 4
                Image(
                    painter = painterResource(id = cameraIconDrawableId),
                    contentDescription = "Image at an angle",
                    modifier = Modifier
                        .size(cameraIconSize.dp)
                        .offset(offset.dp, -offset.dp)
                )
            }
        }
        // Display the background image
        Image(
            painter = painterResource(id = backgroundDrawableId),
            contentDescription = "Background image for ${uiState.babyInfo.theme} theme", // More specific content description
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.FillWidth
        )

        Image(
            painter = painterResource(id = R.drawable.nanit),
            contentDescription = "Nanit logo",
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
    BirthdayScreen(uiState = uiState) // Call the state-driven composable
}


