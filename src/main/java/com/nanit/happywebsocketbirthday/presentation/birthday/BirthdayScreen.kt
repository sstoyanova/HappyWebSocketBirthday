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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nanit.happywebsocketbirthday.R
import com.nanit.happywebsocketbirthday.domain.model.AgeDisplayInfo
import com.nanit.happywebsocketbirthday.domain.model.BabyInfo
import com.nanit.happywebsocketbirthday.ui.theme.AppTheme
import com.nanit.happywebsocketbirthday.ui.theme.DarkBlueTextColor
import kotlin.math.sqrt

@Composable
fun BirthdayScreen(uiState: BirthdayScreenState) {
    val currentTheme = AppTheme.fromThemeName(uiState.babyInfo.theme)

    val backgroundDrawableId = currentTheme.backgroundDrawableId
    val faceDrawableId = currentTheme.faceIconDrawableId
    val cameraIconDrawableId = currentTheme.cameraIconDrawableId
    val backgroundColor = currentTheme.backgroundColor

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
                        contentDescription = "Decoration",
                        modifier = Modifier
                            .width(50.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Image(
                        painter = painterResource(id = uiState.ageDisplayInfo.numberIconDrawableId),
                        contentDescription = "Age Icon",
                        modifier = Modifier
                            .height(88.dp)
                            .padding(start = 22.dp, end = 22.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.right_swirls),
                        contentDescription = "Decoration",
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
                val cameraIconSize = 36
                Image(
                    painter = painterResource(id = faceDrawableId),
                    contentDescription = "Baby Image",
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
        // The background image
        Image(
            painter = painterResource(id = backgroundDrawableId),
            contentDescription = "Background image",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            contentScale = ContentScale.FillWidth
        )
        //Nanit logo
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


@Preview(
    showBackground = true,
//    widthDp = 361,
//    heightDp = 592
) // showBackground is helpful to see the composable against a background

@Composable
fun BirthdayScreenPreview() {
    val sampleBabyInfo = BabyInfo(
        name = "Christiano Ronaldo",
        dob = 1640995200000, // Example DOB (Jan 1, 2022)
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
            BirthdayScreen(uiState = sampleUiState)
        }
    }
}


