package com.campusdigitalfp.tareaflow.ui.screens.pomodoro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.campusdigitalfp.tareaflow.viewmodel.PreferencesViewModel

enum class PomodoroMode { SIMPLE, POMODORO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    navController: NavController,
    prefsViewModel: PreferencesViewModel
) {
    var mode by remember { mutableStateOf(PomodoroMode.SIMPLE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pomodoro") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            PomodoroModeSelector(
                selected = mode,
                onChange = { mode = it }
            )

            Spacer(Modifier.height(20.dp))

            if (mode == PomodoroMode.SIMPLE) {
                Text("Modo temporizador simple", style = MaterialTheme.typography.titleMedium)
                Text("Aquí irá el temporizador ⏳", color = Color.Gray)
            } else {
                Text("Modo Pomodoro completo", style = MaterialTheme.typography.titleMedium)
                Text("Aquí irá el pomodoro completo 🍅", color = Color.Gray)
            }
        }
    }
}


@Composable
fun PomodoroModeSelector(
    selected: PomodoroMode,
    onChange: (PomodoroMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ModeButton(
            text = "Temporizador",
            selected = selected == PomodoroMode.SIMPLE,
            onClick = { onChange(PomodoroMode.SIMPLE) },
            modifier = Modifier.weight(1f)
        )

        ModeButton(
            text = "Pomodoro",
            selected = selected == PomodoroMode.POMODORO,
            onClick = { onChange(PomodoroMode.POMODORO) },
            modifier = Modifier.weight(1f)
        )
    }
}


@Composable
private fun ModeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val fg = if (selected) Color.White else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .clickable { onClick() }
            .padding(6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = fg, fontWeight = FontWeight.Bold)
    }
}