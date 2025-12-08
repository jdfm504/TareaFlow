package com.campusdigitalfp.tareaflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusdigitalfp.tareaflow.data.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data class Success(val message: String = "") : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val state: StateFlow<AuthUiState> = _state

    fun isLoggedIn(): Boolean = repo.currentUser != null

    fun register(email: String, password: String, onSuccess: () -> Unit) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                repo.register(email, password)
                _state.value = AuthUiState.Success("Registro correcto")
                onSuccess()
            } catch (e: Exception) {
                _state.value = AuthUiState.Error(mapFirebaseError(e))
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            try {
                repo.login(email, password)
                _state.value = AuthUiState.Success("Login correcto")
                onSuccess()
            } catch (e: Exception) {
                _state.value = AuthUiState.Error(mapFirebaseError(e))
            }
        }
    }

    fun logout() {
        repo.logout()
        _state.value = AuthUiState.Idle
    }

    private fun mapFirebaseError(e: Exception): String {
        val msg = e.message?.lowercase().orEmpty()
        return when {
            "email address is already in use" in msg -> "Ese correo ya está registrado"
            "badly formatted" in msg || "invalid email" in msg -> "Correo con formato inválido"
            "password is invalid" in msg -> "Contraseña incorrecta"
            "no user record" in msg -> "No existe una cuenta con ese correo"
            else -> "Error: ${e.message ?: "desconocido"}"
        }
    }
    fun resetPassword(email: String, onResult: (Boolean, String) -> Unit) {
        if (email.isBlank()) {
            onResult(false, "Por favor, introduce tu correo electrónico")
            return
        }

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnSuccessListener {
                onResult(true, "Correo de recuperación enviado a $email")
            }
            .addOnFailureListener { e ->
                onResult(false, "Error: ${e.message}")
            }
    }
}