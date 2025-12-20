package com.campusdigitalfp.tareaflow.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.luminance
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.material3.MaterialTheme


@Composable
fun ApplyStatusBarTheme() {
    val systemUiController = rememberSystemUiController()
    val bgColor = MaterialTheme.colorScheme.background
    val darkIcons = bgColor.luminance() > 0.5f

    SideEffect {
        systemUiController.setStatusBarColor(
            color = bgColor,
            darkIcons = darkIcons
        )
    }
}
