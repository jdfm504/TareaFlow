package com.campusdigitalfp.tareaflow.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusdigitalfp.tareaflow.data.model.UserPreferences
import com.campusdigitalfp.tareaflow.ui.screens.pomodoro.PomodoroMode
import com.campusdigitalfp.tareaflow.ui.screens.pomodoro.PomodoroPhase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PomodoroViewModel : ViewModel() {

    var mode by mutableStateOf(PomodoroMode.SIMPLE)
    var phase by mutableStateOf(PomodoroPhase.FOCUS)
    var cycleCount by mutableStateOf(0)
    var isRunning by mutableStateOf(false)

    var remainingSeconds by mutableStateOf(0)
    var totalSeconds by mutableStateOf(0)

    private var timerJob: Job? = null

    // Inicializa el temporizador
    fun initTimer(prefs: UserPreferences) {
        if (totalSeconds == 0) {
            totalSeconds = prefs.pomodoroMinutes * 60
            remainingSeconds = totalSeconds
        }
    }

    // inicio del contador al acaba se llama a onFinish()
    fun start(onFinish: () -> Unit) {
        if (timerJob != null) return
        isRunning = true

        timerJob = viewModelScope.launch {
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
            }
            timerJob = null
            isRunning = false
            onFinish()
        }
    }

    fun pause() {
        timerJob?.cancel()
        timerJob = null
        isRunning = false
    }

    // Reiniciar la fase de concentración
    fun reset(prefs: UserPreferences) {
        pause()
        phase = PomodoroPhase.FOCUS
        cycleCount = 0
        totalSeconds = prefs.pomodoroMinutes * 60
        remainingSeconds = totalSeconds
    }

    // Logica de cambio de fase
    fun goToNextPhase(prefs: UserPreferences) {
        when (phase) {
            PomodoroPhase.FOCUS -> {
                cycleCount++
                phase = if (cycleCount % prefs.cyclesUntilLongBreak == 0) {
                    PomodoroPhase.LONG_BREAK
                } else {
                    PomodoroPhase.SHORT_BREAK
                }
            }
            PomodoroPhase.SHORT_BREAK -> {
                phase = PomodoroPhase.FOCUS
            }
            PomodoroPhase.LONG_BREAK -> {
                phase = PomodoroPhase.FOCUS
                cycleCount = 0
            }
        }

        totalSeconds = when (phase) {
            PomodoroPhase.FOCUS -> prefs.pomodoroMinutes * 60
            PomodoroPhase.SHORT_BREAK -> prefs.shortBreakMinutes * 60
            PomodoroPhase.LONG_BREAK -> prefs.longBreakMinutes * 60
        }
        remainingSeconds = totalSeconds
    }

    // Cambio entre modo temporizador o pomodoro completo
    fun changeMode(prefs: UserPreferences, newMode: PomodoroMode) {
        mode = newMode
        pause()

        when (newMode) {
            PomodoroMode.SIMPLE -> {
                phase = PomodoroPhase.FOCUS
                cycleCount = 0
                totalSeconds = prefs.pomodoroMinutes * 60
                remainingSeconds = totalSeconds
            }
            PomodoroMode.POMODORO -> {
                phase = PomodoroPhase.FOCUS
                cycleCount = 0
                totalSeconds = prefs.pomodoroMinutes * 60
                remainingSeconds = totalSeconds
            }
        }
    }
}
