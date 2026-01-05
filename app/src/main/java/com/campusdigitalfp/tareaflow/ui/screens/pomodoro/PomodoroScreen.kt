package com.campusdigitalfp.tareaflow.ui.screens.pomodoro

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.campusdigitalfp.tareaflow.viewmodel.PomodoroViewModel
import com.campusdigitalfp.tareaflow.viewmodel.PreferencesViewModel
import com.campusdigitalfp.tareaflow.R
import android.media.MediaPlayer
import androidx.compose.ui.platform.LocalContext

enum class PomodoroMode { SIMPLE, POMODORO }

enum class PomodoroPhase {
    FOCUS,        // concentración
    SHORT_BREAK,  // descanso corto
    LONG_BREAK    // descanso largo
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("DefaultLocale")
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

    // Estados controlados en viewmodel
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

    // Diálogo de salida si hay progreso
    var showExitDialog by remember { mutableStateOf(false) }

    fun hasProgress(): Boolean {
        return pomodoroViewModel.isRunning ||
                pomodoroViewModel.remainingSeconds != pomodoroViewModel.totalSeconds ||
                pomodoroViewModel.cycleCount > 0
    }

    fun requestExit() {
        if (hasProgress()) {
            showExitDialog = true
        } else {
            navController.popBackStack()
        }
    }

    BackHandler {
        requestExit()
    }

    // Ayuda contextual según el modo simple o pomodoro del temporizador
    var showHelp by remember { mutableStateOf(false) }

    if (showHelp) {

        val helpTitle = if (mode == PomodoroMode.SIMPLE)
            stringResource(R.string.pomodoro_help_simple_title)
        else
            stringResource(R.string.pomodoro_help_full_title)

        val helpText = if (mode == PomodoroMode.SIMPLE)
            stringResource(R.string.pomodoro_help_simple_body)
        else
            stringResource(R.string.pomodoro_help_full_body)

        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text(helpTitle) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(helpText)
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) {
                    Text(stringResource(R.string.pomodoro_help_button))
                }
            }
        )
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.pomodoro_exit_title)) },
            text = { Text(stringResource(R.string.pomodoro_exit_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        pomodoroViewModel.reset(prefs)
                        navController.popBackStack()
                    }
                ) {
                    Text(stringResource(R.string.dialog_yes_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    // Para la reproducción de sonidos en los cambios de fase
    val context = LocalContext.current

    fun playSound(resId: Int) {
        MediaPlayer.create(context, resId)?.apply {
            setOnCompletionListener { release() }
            start()
        }
    }

    // Helper para fin de fase
    fun handlePhaseFinish() {
        if (pomodoroViewModel.mode == PomodoroMode.POMODORO) {
            if (prefs.soundEnabled) {
                // Determinar qué sonido reproducir según la fase que TERMINA
                when (phase) {
                    PomodoroPhase.FOCUS -> playSound(R.raw.focus_end)
                    PomodoroPhase.SHORT_BREAK -> playSound(R.raw.short_break_end)
                    PomodoroPhase.LONG_BREAK -> playSound(R.raw.long_break_end)
                }
            }
            pomodoroViewModel.goToNextPhase(prefs)

            if (prefs.autoStartNextPhase) {
                pomodoroViewModel.start {
                    handlePhaseFinish()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pomodoro_title)) },
                navigationIcon = {
                    IconButton(onClick = { requestExit() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { showHelp = true }) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = stringResource(R.string.pomodoro_help_icon_cd)
                        )
                    }
                }
            )
        }
    ) { padding ->

        val configuration = LocalConfiguration.current
        val isLandscape =
            configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val onPhaseIconClick: () -> Unit = {
            if (mode == PomodoroMode.POMODORO) {
                pomodoroViewModel.pause()
                pomodoroViewModel.goToNextPhase(prefs)
            }
        }

        if (isLandscape) {
            PomodoroLandscapeLayout(
                padding = padding,
                mode = mode,
                phase = phase,
                phaseColor = phaseColor,
                phaseIcon = phaseIcon,
                cycleCount = cycleCount,
                progress = progress,
                timeText = timeText,
                isRunning = isRunning,
                onStart = {
                    pomodoroViewModel.start { handlePhaseFinish() }
                },
                onPause = { pomodoroViewModel.pause() },
                onResetTimer = { pomodoroViewModel.reset(prefs) },
                onResetCycles = { pomodoroViewModel.resetCycles(prefs) },
                onModeChange = { newMode ->
                    pomodoroViewModel.changeMode(prefs, newMode)
                },
                onPhaseIconClick = onPhaseIconClick
            )
        } else {
            PomodoroPortraitLayout(
                padding = padding,
                mode = mode,
                phase = phase,
                phaseColor = phaseColor,
                phaseIcon = phaseIcon,
                cycleCount = cycleCount,
                progress = progress,
                timeText = timeText,
                isRunning = isRunning,
                onStart = {
                    pomodoroViewModel.start { handlePhaseFinish() }
                },
                onPause = { pomodoroViewModel.pause() },
                onResetTimer = { pomodoroViewModel.reset(prefs) },
                onResetCycles = { pomodoroViewModel.resetCycles(prefs) },
                onModeChange = { newMode ->
                    pomodoroViewModel.changeMode(prefs, newMode)
                },
                onPhaseIconClick = onPhaseIconClick
            )
        }
    }
}

// Pantalla en vertical
@Composable
private fun PomodoroPortraitLayout(
    padding: PaddingValues,
    mode: PomodoroMode,
    phase: PomodoroPhase,
    phaseColor: Color,
    phaseIcon: ImageVector,
    cycleCount: Int,
    progress: Float,
    timeText: String,
    isRunning: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResetTimer: () -> Unit,
    onResetCycles: () -> Unit,
    onModeChange: (PomodoroMode) -> Unit,
    onPhaseIconClick: () -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp

    val timerSize = when {
        screenHeight < 600 -> 220.dp     // pantallas pequeñas
        screenHeight < 750 -> 260.dp     // medianas
        else -> 320.dp                   // grandes
    }

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        // Icono de fase centrado
        Icon(
            imageVector = phaseIcon,
            contentDescription = null,
            tint = phaseColor,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(Alignment.CenterHorizontally)
                .size(60.dp)
                .let { base ->
                    if (mode == PomodoroMode.POMODORO) {
                        base.clickable { onPhaseIconClick() }
                    } else {
                        base
                    }
                }
        )

        Spacer(Modifier.height(12.dp))

        // Cambio de modo
        PomodoroModeSelector(
            selected = mode,
            onChange = onModeChange
        )

        Spacer(Modifier.height(20.dp))

        // Texto fase / ciclos en modo completo
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.pomodoro_cycle_label, cycleCount),
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedButton(
                    onClick = onResetCycles
                ) {
                    Text(
                        text = stringResource(R.string.pomodoro_reset_cycles),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

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
            modifier = Modifier
                .size(timerSize)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(30.dp))

        // Controles
        PomodoroControls(
            isRunning = isRunning,
            onStart = onStart,
            onPause = onPause,
            onReset = onResetTimer
        )
    }
}

// Para pantalla en horizontal
@Composable
private fun PomodoroLandscapeLayout(
    padding: PaddingValues,
    mode: PomodoroMode,
    phase: PomodoroPhase,
    phaseColor: Color,
    phaseIcon: ImageVector,
    cycleCount: Int,
    progress: Float,
    timeText: String,
    isRunning: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResetTimer: () -> Unit,
    onResetCycles: () -> Unit,
    onModeChange: (PomodoroMode) -> Unit,
    onPhaseIconClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = phaseIcon,
                contentDescription = null,
                tint = phaseColor,
                modifier = Modifier
                    .size(64.dp)
                    .let { base ->
                        if (mode == PomodoroMode.POMODORO) {
                            base.clickable { onPhaseIconClick() }
                        } else base
                    }
            )

            PomodoroModeSelector(
                selected = mode,
                onChange = onModeChange
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val screenHeight = LocalConfiguration.current.screenHeightDp

            val timerSize = when {
                screenHeight < 600 -> 220.dp     // pantallas pequeñas
                screenHeight < 750 -> 260.dp     // medianas
                else -> 320.dp                   // grandes
            }

            // IZQUIERDA: Fase + ciclos + reset
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (mode == PomodoroMode.POMODORO) {
                    val phaseText = when (phase) {
                        PomodoroPhase.FOCUS -> stringResource(R.string.pomodoro_phase_focus)
                        PomodoroPhase.SHORT_BREAK -> stringResource(R.string.pomodoro_phase_short_break)
                        PomodoroPhase.LONG_BREAK -> stringResource(R.string.pomodoro_phase_long_break)
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        phaseText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.pomodoro_cycle_label, cycleCount),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onResetCycles
                    ) {
                        Text(text = stringResource(R.string.pomodoro_reset_cycles))
                    }
                } else {
                    Text(
                        stringResource(R.string.pomodoro_simple_mode_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            // CENTRO: Reloj
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularTimer(
                    progress = progress,
                    timeText = timeText,
                    ringColor = phaseColor,
                    modifier = Modifier
                        .size(timerSize)
                        .align(Alignment.Center)
                )
            }

            // DERECHA: Controles
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PomodoroControls(
                    isRunning = isRunning,
                    onStart = onStart,
                    onPause = onPause,
                    onReset = onResetTimer
                )
            }
        }
    }
}


// Selector de temporizador
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
            text = stringResource(R.string.pomodoro_mode_full),
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


// Controles
@Composable
fun PomodoroControls(
    isRunning: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Botón Start / Pause
        Box(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary)
                .clickable { if (isRunning) onPause() else onStart() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isRunning)
                    stringResource(R.string.pomodoro_btn_pause)
                else
                    stringResource(R.string.pomodoro_btn_start),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        // Botón Reset
        Box(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.secondary)
                .clickable { onReset() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.pomodoro_btn_reset),
                color = MaterialTheme.colorScheme.onSecondary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}