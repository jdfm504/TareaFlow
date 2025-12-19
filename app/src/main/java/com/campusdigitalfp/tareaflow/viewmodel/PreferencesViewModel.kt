package com.campusdigitalfp.tareaflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusdigitalfp.tareaflow.data.PreferencesRepository
import com.campusdigitalfp.tareaflow.data.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PreferencesViewModel : ViewModel() {

    private val repo = PreferencesRepository()

    private val _prefs = MutableStateFlow(UserPreferences())
    val prefs: StateFlow<UserPreferences> get() = _prefs

    init {
        loadUserPreferences()
    }

    fun loadUserPreferences() {
        viewModelScope.launch {
            _prefs.value = repo.loadPreferences()
        }
    }

    fun setDarkTheme(value: Boolean) {
        viewModelScope.launch {
            repo.updateDarkTheme(value)
            _prefs.value = _prefs.value.copy(darkTheme = value)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            repo.updateLanguage(lang)
            _prefs.value = _prefs.value.copy(language = lang)
        }
    }

    fun setPomodoro(minutes: Int) {
        viewModelScope.launch {
            repo.updatePomodoro(minutes)
            _prefs.value = _prefs.value.copy(pomodoroMinutes = minutes)
        }
    }
}
