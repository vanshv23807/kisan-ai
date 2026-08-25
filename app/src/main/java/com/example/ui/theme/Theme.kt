package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryGreen,
    onPrimary = Color(0xFF002114),
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = Color(0xFFA8E7C5),
    secondary = DarkPrimaryGreen,
    onSecondary = Color(0xFF002114),
    secondaryContainer = Color(0xFF264E3D),
    onSecondaryContainer = Color(0xFFCFFBE2),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = AlertRed,
    errorContainer = Color(0xFF4A1010),
    onError = Color.White,
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = TextOnPrimary,
    primaryContainer = PrimaryContainerGreen,
    onPrimaryContainer = PrimaryGreenLight,
    secondary = SecondaryGreen,
    onSecondary = Color.White,
    secondaryContainer = SecondaryContainerGreen,
    onSecondaryContainer = Color(0xFF002114),
    background = SoftBackground,
    onBackground = TextPrimary,
    surface = SurfaceLowest,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceContainer,
    onSurfaceVariant = TextSecondary,
    outline = BorderLight,
    error = AlertRed,
    errorContainer = AlertRedContainer,
    onError = Color.White,
    onErrorContainer = AlertRedText
)

@Composable
fun KisanAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
