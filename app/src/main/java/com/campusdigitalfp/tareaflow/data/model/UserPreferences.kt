package com.campusdigitalfp.tareaflow.data.model

// Preferencias de los usuarios
data class UserPreferences(
    val darkTheme: Boolean = false,
    val language: String = "es",
    val pomodoroMinutes: Int = 30,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val cyclesUntilLongBreak: Int = 4
)