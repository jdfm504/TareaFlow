package com.campusdigitalfp.tareaflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusdigitalfp.tareaflow.data.PreferencesRepository
import com.campusdigitalfp.tareaflow.data.model.UserPreferences
import com.google.firebase.auth.FirebaseAuth
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
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser

            // si no hay usuario logueado → usar prefs por defecto
            if (user == null) {
                _prefs.value = UserPreferences()   // valores por defecto
                _isLoaded.value = true             // dejar pasar a la UI
                return@launch
            }

            // si hay usuario → usamos Firestore
            repo.ensureDocumentExists()

            repo.listenPreferences().collect { loaded ->
                _prefs.value = loaded
                _isLoaded.value = true
            }
        }
    }
    fun setDarkTheme(value: Boolean) {
        viewModelScope.launch { repo.updateDarkTheme(value) }
    }
    fun setPomodoro(minutes: Int) {
        viewModelScope.launch { repo.updatePomodoro(minutes) }
    }

    fun setShortBreak(minutes: Int) {
        viewModelScope.launch { repo.updateShortBreak(minutes) }
    }

    fun setLongBreak(minutes: Int) {
        viewModelScope.launch { repo.updateLongBreak(minutes) }
    }

    fun setCycles(count: Int) {
        viewModelScope.launch { repo.updateCycles(count) }
    }

    fun reload() {
        viewModelScope.launch {
            val auth = FirebaseAuth.getInstance()

            if (auth.currentUser == null) {
                _prefs.value = UserPreferences()
                _isLoaded.value = true
                return@launch
            }

            repo.ensureDocumentExists()

            repo.listenPreferences().collect { loaded ->
                _prefs.value = loaded
                _isLoaded.value = true
            }
        }
    }

    fun clear() {
        _prefs.value = UserPreferences()
        _isLoaded.value = false
    }

    fun setAutoStart(value: Boolean) {
        viewModelScope.launch { repo.updateAutoStart(value) }
    }

    fun markPhaseTipShown() {
        viewModelScope.launch { repo.updatePhaseTipShown() }
    }

    fun updateSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repo.updateSoundEnabled(enabled)
        }
    }
}

