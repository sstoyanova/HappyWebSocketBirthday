package com.nanit.happywebsocketbirthday.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.nanit.happywebsocketbirthday.R

@Suppress("unused")
enum class AppTheme(
    val themeName: String,
    @DrawableRes val backgroundDrawableId: Int,
    @DrawableRes val faceIconDrawableId: Int,
    @DrawableRes val cameraIconDrawableId: Int,
    val backgroundColor: Color,
    val darkColor: Color
) {
    PELICAN(
        themeName = "pelican",
        backgroundDrawableId = R.drawable.bg_android_pelican,
        faceIconDrawableId = R.drawable.blue_face,
        cameraIconDrawableId = R.drawable.ic_blue_camera,
        backgroundColor = PelicanBlue,
        darkColor = DarkPelicanBlue
    ),
    FOX(
        themeName = "fox",
        backgroundDrawableId = R.drawable.bg_android_fox,
        faceIconDrawableId = R.drawable.yellow_face,
        cameraIconDrawableId = R.drawable.ic_yellow_camera,
        backgroundColor = FoxOrange,
        darkColor = DarkFoxOrange
    ),
    ELEPHANT(
        themeName = "elephant",
        backgroundDrawableId = R.drawable.bg_android_elephant,
        faceIconDrawableId = R.drawable.green_face,
        cameraIconDrawableId = R.drawable.ic_teal_camera,
        backgroundColor = ElephantTeal,
        darkColor = DarkElephantTeal
    );

    companion object {
        // Helper function to get the AppTheme based on the theme name string
        fun fromThemeName(themeName: String?): AppTheme {
            return entries.firstOrNull { it.themeName == themeName }
                ?: PELICAN // Default to PELICAN if name is unknown or null
        }
    }
}