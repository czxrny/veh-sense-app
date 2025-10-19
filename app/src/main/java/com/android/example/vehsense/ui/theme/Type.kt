package com.android.example.vehsense.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.android.example.vehsense.R

val CourierNew = FontFamily(
    Font(R.font.courier_new, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.courier_new_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.courier_new_italic, FontWeight.Normal, FontStyle.Italic)
)

val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = CourierNew,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    titleLarge = TextStyle(
        fontFamily = CourierNew,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )
)
