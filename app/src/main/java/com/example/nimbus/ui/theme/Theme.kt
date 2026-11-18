package com.example.nimbus.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NightBlue = Color(0xFF0B1A33)
val PanelBlue = Color(0xFF16294D)
val CloudWhite = Color(0xFFEDEFF5)
val StarYellow = Color(0xFFF2C94C)
val GrassGreen = Color(0xFF5FA36A)

private val NimbusColors = darkColorScheme(
    primary = StarYellow,
    onPrimary = NightBlue,
    secondary = GrassGreen,
    onSecondary = NightBlue,
    background = NightBlue,
    onBackground = CloudWhite,
    surface = PanelBlue,
    onSurface = CloudWhite,
    surfaceVariant = PanelBlue,
    onSurfaceVariant = CloudWhite,
    surfaceContainer = PanelBlue,
    surfaceContainerHigh = PanelBlue,
    surfaceContainerHighest = PanelBlue,
    outline = StarYellow
)

@Composable
fun NimbusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NimbusColors,
        typography = Typography,
        content = content
    )
}