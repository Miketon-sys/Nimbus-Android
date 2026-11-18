package com.example.nimbus.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily

private val Default = Typography()

val Typography = Typography(
    displayMedium = Default.displayMedium.copy(fontFamily = FontFamily.Serif),
    headlineMedium = Default.headlineMedium.copy(fontFamily = FontFamily.Serif),
    titleLarge = Default.titleLarge.copy(fontFamily = FontFamily.Serif),
    titleMedium = Default.titleMedium.copy(fontFamily = FontFamily.Serif)
)