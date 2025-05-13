package com.nanit.happywebsocketbirthday.presentation.birthday

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nanit.happywebsocketbirthday.R
import com.nanit.happywebsocketbirthday.ui.theme.DarkBlueTextColor


/**
 * A composable function that displays the baby's name and age information.
 *
 * This function renders the baby's name, an age icon, and a text label for the age,
 * formatted according to the provided [AgeDisplayInfo].
 *
 * @param name The name of the baby to display.
 * @param ageDisplayInfo An [AgeDisplayInfo] object containing details about how to display the age,
 *   including the plural resource ID for the age label, the age value itself,
 *   and the drawable resource ID for the age icon.
 */
@Composable
fun AgeNameDisplay(name: String, ageDisplayInfo: AgeDisplayInfo) {
    val babyNameLabel = stringResource(R.string.today_baby_name_is_label, name)
    val ageLabelText = pluralStringResource(
        id = ageDisplayInfo.ageLabelPluralResId, // Use the plural resource ID
        ageDisplayInfo.age // Directly use the age from AgeDisplayInfo as the quantity
    )

    val bentonSansFamily = FontFamily(
        Font(R.font.benton_sans_regular, FontWeight.Normal),
        Font(R.font.benton_sans_bold, FontWeight.Bold)
    )

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
            painter = painterResource(id = ageDisplayInfo.numberIconDrawableId),
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

