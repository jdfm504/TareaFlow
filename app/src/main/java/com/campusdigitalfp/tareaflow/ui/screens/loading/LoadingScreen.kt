package com.campusdigitalfp.tareaflow.ui.screens.loading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LoadingScreen() {

    // Verde oscuro suave que no molesta en ningún tema
    val GreenDeep = Color(0xFF0F3324)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenDeep),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 4.dp,
            modifier = Modifier.size(48.dp)
        )
    }
}
