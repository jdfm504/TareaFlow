package com.campusdigitalfp.tareaflow.data.model

data class UserPreferences(
    val darkTheme: Boolean = false,
    val language: String = "es",
    val pomodoroMinutes: Int = 30
)