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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.campusdigitalfp.tareaflow.viewmodel.PreferencesViewModel
import kotlinx.coroutines.*
import kotlin.times


enum class PomodoroMode { SIMPLE, POMODORO }

enum class PomodoroPhase {
    FOCUS,        // concentración
    SHORT_BREAK,  // descanso corto
    LONG_BREAK    // descanso largo
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    navController: NavController,
    prefsViewModel: PreferencesViewModel
) {
    val prefs = prefsViewModel.prefs.collectAsState().value

    var mode by remember { mutableStateOf(PomodoroMode.SIMPLE) }
    var phase by remember { mutableStateOf(PomodoroPhase.FOCUS) }
    var cycleCount by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }
    var timerJob by remember { mutableStateOf<Job?>(null) }

    // Tiempo inicial según fase
    fun initialSeconds(): Int = when (phase) {
        PomodoroPhase.FOCUS -> prefs.pomodoroMinutes * 60
        PomodoroPhase.SHORT_BREAK -> prefs.shortBreakMinutes * 60
        PomodoroPhase.LONG_BREAK -> prefs.longBreakMinutes * 60
    }

    var remainingSeconds by remember { mutableStateOf(initialSeconds()) }

    var progress by remember { mutableStateOf(1f) }
    var timeText by remember { mutableStateOf("00:00") }

    //  Actualizar el tiempo
    LaunchedEffect(remainingSeconds, phase, prefs) {
        val total = initialSeconds().toFloat()
        progress = remainingSeconds / total
        timeText = String.format("%02d:%02d", remainingSeconds / 60, remainingSeconds % 60)
    }

    // Color segun la fase del pomodoro
    val phaseColor = when (phase) {
        PomodoroPhase.FOCUS -> Color(0xFFE53935)        // rojo
        PomodoroPhase.SHORT_BREAK -> Color(0xFF42A5F5)  // azul
        PomodoroPhase.LONG_BREAK -> Color(0xFF66BB6A)   // verde
    }

    val phaseIcon = when (phase) {
        PomodoroPhase.FOCUS -> Icons.Rounded.Timer
        PomodoroPhase.SHORT_BREAK -> Icons.Rounded.Coffee
        PomodoroPhase.LONG_BREAK -> Icons.Rounded.Bedtime
    }

    val prefsState by prefsViewModel.prefs.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(mode) {
        if (mode == PomodoroMode.POMODORO && !prefsState.phaseTipShown) {
            snackbarHostState.showSnackbar(
                "Pulsa el icono para cambiar manualmente de fase"
            )
            prefsViewModel.markPhaseTipShown()
        }
    }


    var showHelp by remember { mutableStateOf(false) }

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("¿Cómo funciona el método Pomodoro?") },
            text = {
                Text(
                    "● 25 minutos de concentración\n" +
                            "● 5 minutos de descanso corto\n" +
                            "● Cada 4 ciclos → descanso largo\n\n" +
                            "Puedes tocar el icono superior para cambiar la fase manualmente."
                )
            },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    //  cambio de fase
    fun nextPhase() {
        when (phase) {
            PomodoroPhase.FOCUS -> {
                cycleCount++
                phase = if (cycleCount % prefs.cyclesUntilLongBreak == 0)
                    PomodoroPhase.LONG_BREAK
                else PomodoroPhase.SHORT_BREAK
            }
            PomodoroPhase.SHORT_BREAK -> {
                phase = PomodoroPhase.FOCUS
            }
            PomodoroPhase.LONG_BREAK -> {
                phase = PomodoroPhase.FOCUS
                cycleCount = 0
            }
        }
        remainingSeconds = initialSeconds()
    }

    // control de temporizador
    fun startTimer() {
        if (timerJob != null) return
        isRunning = true

        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
            }
            timerJob = null
            isRunning = false

            if (mode == PomodoroMode.POMODORO) {
                nextPhase()
                if (prefs.autoStartNextPhase) {
                    startTimer()
                } else {
                    isRunning = false
                }
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null
        isRunning = false
    }

    fun resetTimer() {
        pauseTimer()                         // detener
        phase = PomodoroPhase.FOCUS          // volver a foco
        cycleCount = 0                       // reiniciar ciclos
        remainingSeconds = prefs.pomodoroMinutes * 60  // tiempo inicial
    }

    @SuppressLint("DefaultLocale")
    fun resetSimple() {
        pauseTimer()
        remainingSeconds = prefs.pomodoroMinutes * 60
        timeText = String.format(
            "%02d:%02d", remainingSeconds / 60, remainingSeconds % 60
        )
        progress = 1f
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Pomodoro") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { showHelp = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Ayuda")
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

            Icon(
                imageVector = phaseIcon,
                contentDescription = null,
                tint = phaseColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
                    .size(60.dp)
                    .clickable {
                        if (mode == PomodoroMode.POMODORO) {

                            pauseTimer()     // si estaba iniciado, parar
                            nextPhase()      // cambiar fase correctamente

                            remainingSeconds = initialSeconds()   // actualizar el tiempo
                        }
                    }
            )

            PomodoroModeSelector(
                selected = mode,
                onChange = { newMode ->
                    mode = newMode
                    pauseTimer()

                    phase = PomodoroPhase.FOCUS
                    cycleCount = 0
                    remainingSeconds = prefs.pomodoroMinutes * 60
                    progress = 1f
                    timeText = String.format("%02d:%02d", prefs.pomodoroMinutes, 0)
                }
            )

            Spacer(Modifier.height(20.dp))

            // modo simple
            if (mode == PomodoroMode.SIMPLE) {

                Text("Modo temporizador simple", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(30.dp))

                CircularTimer(
                    progress = progress,
                    timeText = timeText,
                    ringColor = phaseColor,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(30.dp))

                PomodoroControls(
                    isRunning = isRunning,
                    onStart = { startTimer() },
                    onPause = { pauseTimer() },
                    onReset = { resetSimple() }
                )
            }

            // modo completo
            else {

                val phaseText = when (phase) {
                    PomodoroPhase.FOCUS -> "Concentración"
                    PomodoroPhase.SHORT_BREAK -> "Descanso corto"
                    PomodoroPhase.LONG_BREAK -> "Descanso largo"
                }

                Text(
                    phaseText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Ciclo $cycleCount",
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(30.dp))

                CircularTimer(
                    progress = progress,
                    timeText = timeText,
                    ringColor = phaseColor,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(30.dp))

                PomodoroControls(
                    isRunning = isRunning,
                    onStart = { startTimer() },
                    onPause = { pauseTimer() },
                    onReset = { resetTimer() }
                )
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
            text = if (isRunning) "Pausar" else "Iniciar",
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
            text = "Reiniciar",
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