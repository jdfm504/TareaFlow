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

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> get() = _isLoaded

    init {
        viewModelScope.launch {
            repo.ensureDocumentExists()
            repo.listenPreferences().collect { loaded ->
                _prefs.value = loaded
                _isLoaded.value = true   // Ya tenemos datos se muestra UI
            }
        }
    }

    fun setDarkTheme(value: Boolean) {
        viewModelScope.launch { repo.updateDarkTheme(value) }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch { repo.updateLanguage(lang) }
    }

    fun setPomodoro(minutes: Int) {
        viewModelScope.launch { repo.updatePomodoro(minutes) }
    }
}


