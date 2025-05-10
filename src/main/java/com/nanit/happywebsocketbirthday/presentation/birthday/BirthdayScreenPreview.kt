package com.nanit.happywebsocketbirthday.presentation.birthday

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.nanit.happywebsocketbirthday.R
import com.nanit.happywebsocketbirthday.domain.model.AgeDisplayInfo
import com.nanit.happywebsocketbirthday.domain.model.BabyInfo

@Preview(
    showBackground = true,
//    widthDp = 361,
//    heightDp = 592
) // showBackground is helpful to see the composable against a background

@Composable
fun BirthdayScreenPreview() {
    // Provide a sample UI state for the preview
    val sampleBabyInfo = BabyInfo(
        name = "Christiano Ronaldo",
        dob = 1640995200000, // Example DOB (Jan 1, 2022)
        theme = "pelican" // Set a sample theme
    )

    val sampleUiState = BirthdayScreenState(
        babyInfo = sampleBabyInfo,
        isLoading = false,
        errorMessage = null,
        ageDisplayInfo = AgeDisplayInfo(R.drawable.icon_10, "MONTHS OLD!")
    )

    MaterialTheme {
        Surface {
            BirthdayScreen(uiState = sampleUiState)
        }
    }
}