package com.campusdigitalfp.tareaflow.ui.screens.pomodoro

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.campusdigitalfp.tareaflow.viewmodel.PomodoroViewModel
import com.campusdigitalfp.tareaflow.viewmodel.PreferencesViewModel
import kotlinx.coroutines.*
import com.campusdigitalfp.tareaflow.R

enum class PomodoroMode { SIMPLE, POMODORO }

enum class PomodoroPhase {
    FOCUS,        // concentración
    SHORT_BREAK,  // descanso corto
    LONG_BREAK    // descanso largo
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    navController: NavController,
    prefsViewModel: PreferencesViewModel,
    pomodoroViewModel: PomodoroViewModel = viewModel()
) {
    val prefs = prefsViewModel.prefs.collectAsState().value

    // Inicializar si es primer arranque
    LaunchedEffect(Unit) {
        pomodoroViewModel.initTimer(prefs)
    }

    // Posibles estados controlados en viewmodel
    val mode = pomodoroViewModel.mode
    val phase = pomodoroViewModel.phase
    val isRunning = pomodoroViewModel.isRunning
    val cycleCount = pomodoroViewModel.cycleCount
    val remainingSeconds = pomodoroViewModel.remainingSeconds
    val totalSeconds = pomodoroViewModel.totalSeconds

    val progress = if (totalSeconds > 0) {
        remainingSeconds.toFloat() / totalSeconds.toFloat()
    } else {
        1f
    }

    val timeText = String.format(
        "%02d:%02d",
        remainingSeconds / 60,
        remainingSeconds % 60
    )

    // Colores para cada fase
    val phaseColor = when (phase) {
        PomodoroPhase.FOCUS -> Color(0xFFE53935)        // rojo
        PomodoroPhase.SHORT_BREAK -> Color(0xFF42A5F5)  // azul
        PomodoroPhase.LONG_BREAK -> Color(0xFF66BB6A)   // verde
    }

    // Iconos para cada fase
    val phaseIcon = when (phase) {
        PomodoroPhase.FOCUS -> Icons.Rounded.Timer
        PomodoroPhase.SHORT_BREAK -> Icons.Rounded.Coffee
        PomodoroPhase.LONG_BREAK -> Icons.Rounded.Bedtime
    }

    // Mensaje de consejo sobre pomodoro
    val snackbarHostState = remember { SnackbarHostState() }
    val prefsState by prefsViewModel.prefs.collectAsState()

    val phaseTipText = stringResource(R.string.pomodoro_snackbar_phase_tip)

    LaunchedEffect(mode) {
        if (mode == PomodoroMode.POMODORO && !prefsState.phaseTipShown) {
            snackbarHostState.showSnackbar(phaseTipText)
            prefsViewModel.markPhaseTipShown()
        }
    }

    // Ayuda sobre pomodoro
    var showHelp by remember { mutableStateOf(false) }

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = {  Text(stringResource(R.string.pomodoro_help_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.pomodoro_help_body,
                        prefs.pomodoroMinutes,
                        prefs.shortBreakMinutes,
                        prefs.cyclesUntilLongBreak
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) {
                    Text(stringResource(R.string.pomodoro_help_button))
                }
            }
        )
    }

    // Helper para fin de fase
    fun handlePhaseFinish() {
        if (pomodoroViewModel.mode == PomodoroMode.POMODORO) {
            pomodoroViewModel.goToNextPhase(prefs)

            if (prefs.autoStartNextPhase) {
                pomodoroViewModel.start {
                    handlePhaseFinish()
                }
            }
        }
    }

    // Pantalla principal de pomodoro
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pomodoro_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { showHelp = true }) {
                        Icon(Icons.Default.Info, stringResource(R.string.pomodoro_help_icon_cd))
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

            // Icono según fase
            Icon(
                imageVector = phaseIcon,
                contentDescription = null,
                tint = phaseColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .size(60.dp)
                    .let { base ->
                        // Solo clicable en modo Pomodoro completo
                        if (mode == PomodoroMode.POMODORO) {
                            base.clickable {
                                pomodoroViewModel.goToNextPhase(prefs)


                            }
                        } else {
                            base
                        }
                    }
            )

            Spacer(Modifier.height(12.dp))

            // Cambio de modo
            PomodoroModeSelector(
                selected = mode,
                onChange = { newMode ->
                    pomodoroViewModel.changeMode(prefs, newMode)
                }
            )

            Spacer(Modifier.height(20.dp))

            // Texto fase / ciclo solo en modo completo
            if (mode == PomodoroMode.POMODORO) {
                val phaseText = when (phase) {
                    PomodoroPhase.FOCUS -> stringResource(R.string.pomodoro_phase_focus)
                    PomodoroPhase.SHORT_BREAK -> stringResource(R.string.pomodoro_phase_short_break)
                    PomodoroPhase.LONG_BREAK -> stringResource(R.string.pomodoro_phase_long_break)
                }

                Text(
                    phaseText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.pomodoro_cycle_label, cycleCount),
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(16.dp))
            } else {
                Text(
                    stringResource(R.string.pomodoro_simple_mode_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(16.dp))
            }

            // Temporizador
            CircularTimer(
                progress = progress,
                timeText = timeText,
                ringColor = phaseColor,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(30.dp))

            // Controles
            PomodoroControls(
                isRunning = isRunning,
                onStart = {
                    pomodoroViewModel.start {
                        handlePhaseFinish()
                    }
                },
                onPause = { pomodoroViewModel.pause() },
                onReset = { pomodoroViewModel.reset(prefs) }
            )
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
            text = stringResource(R.string.pomodoro_mode_simple),
            selected = selected == PomodoroMode.SIMPLE,
            onClick = { onChange(PomodoroMode.SIMPLE) },
            modifier = Modifier.weight(1f)
        )

        ModeButton(
            text =  stringResource(R.string.pomodoro_mode_full),
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

@Composable
fun PomodoroControls(
    isRunning: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {

        // Iniciar / Pausar
        Text(
            text = if (isRunning)
                stringResource(R.string.pomodoro_btn_pause)
            else
                stringResource(R.string.pomodoro_btn_start),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { if (isRunning) onPause() else onStart() }
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 32.dp, vertical = 12.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        // Reiniciar
        Text(
            text = stringResource(R.string.pomodoro_btn_reset),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onReset() }
                .background(MaterialTheme.colorScheme.secondary)
                .padding(horizontal = 32.dp, vertical = 12.dp),
            color = MaterialTheme.colorScheme.onSecondary,
            fontWeight = FontWeight.Bold
        )
    }
}