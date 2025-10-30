package com.campusdigitalfp.tareaflow.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimary,
    secondary = BlueSecondary,
    tertiary = BlueTertiary,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = OnPrimaryText,
    onBackground = Color(0xFFECECEC),
    onSurface = Color(0xFFECECEC)
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    secondary = BlueSecondary,
    tertiary = BlueTertiary,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = OnPrimaryText,
    onBackground = OnBackgroundText,
    onSurface = OnBackgroundText
)

@Composable
fun TareaFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        //dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        //    val context = LocalContext.current
        //    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        //}
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
