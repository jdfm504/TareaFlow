package com.campusdigitalfp.tareaflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusdigitalfp.tareaflow.data.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.campusdigitalfp.tareaflow.R

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data object Success : AuthUiState()
    data class Error(val messageRes: Int) : AuthUiState()
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

                _state.value = AuthUiState.Success
                onSuccess()

            } catch (e: Exception) {
                _state.value = AuthUiState.Error(mapFirebaseErrorRes(e))
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        _state.value = AuthUiState.Loading

        viewModelScope.launch {
            try {
                repo.login(email, password)
                _state.value = AuthUiState.Success
                onSuccess()
            } catch (e: Exception) {
                _state.value = AuthUiState.Error(mapFirebaseErrorRes(e))
            }
        }
    }

    fun logout() {
        repo.logout()
        _state.value = AuthUiState.Idle
    }

    // ------------------------------------------------------
    // mapea errores a IDs de strings.xml
    // ------------------------------------------------------
    private fun mapFirebaseErrorRes(e: Exception): Int {
        val msg = e.message?.lowercase().orEmpty()

        return when {
            "email address is already in use" in msg ->
                R.string.error_email_in_use

            "badly formatted" in msg || "invalid email" in msg ->
                R.string.error_email_format

            "password is invalid" in msg ->
                R.string.error_wrong_password

            "no user record" in msg ->
                R.string.error_no_account

            else ->
                R.string.error_unknown
        }
    }
    // ------------------------------------------------------
    // Reset password
    // ------------------------------------------------------
    fun resetPassword(email: String, onResult: (Boolean, Int) -> Unit) {
        if (email.isBlank()) {
            onResult(false, R.string.error_reset_email_empty)
            return
        }

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnSuccessListener {
                onResult(true, R.string.reset_email_sent)
            }
            .addOnFailureListener { e ->
                onResult(false, R.string.error_unknown)
            }
    }
}