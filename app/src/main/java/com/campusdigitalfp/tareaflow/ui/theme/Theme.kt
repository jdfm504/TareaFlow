package com.campusdigitalfp.tareaflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    secondary = BlueSecondary,
    tertiary = BlueTertiary,
    background = Color(0xFF121212),
    onBackground = CardDarkTextPrimary,
    surface = CardDarkBackground,
    onSurface = CardDarkTextPrimary,
    surfaceVariant = CardDarkBackground,
    onSurfaceVariant = CardDarkTextSecondary,
    outline = CardDarkBorder,
    error = Color(0xFFD01111),
    primaryContainer = GreenLight,
    onPrimaryContainer = OnBackgroundText,
    secondaryContainer = BlueSecondary.copy(alpha = 0.2f),
    onError = GreenLight
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = OnPrimaryText,
    secondary = BlueSecondary,
    tertiary = BlueTertiary,
    background = LightBackground,
    onBackground = OnBackgroundText,
    surface = CardLightBackground,
    onSurface = CardLightTextPrimary,
    surfaceVariant = CardBackground,
    onSurfaceVariant = CardTextPrimary,
    outline = CardLightBorder,
    primaryContainer = GreenDark,
    onPrimaryContainer = Color.White,
    secondaryContainer = BlueSecondary.copy(alpha = 0.25f)
)

@Composable
fun TareaFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
