package com.campusdigitalfp.tareaflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusdigitalfp.tareaflow.data.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.campusdigitalfp.tareaflow.R
import com.campusdigitalfp.tareaflow.data.UserProfileRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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

    fun register(email: String, password: String, name: String, onSuccess: () -> Unit) {
        _state.value = AuthUiState.Loading

        viewModelScope.launch {
            try {
                val auth = FirebaseAuth.getInstance()

                if (auth.currentUser?.isAnonymous == true) {
                    // Caso 1: era anónimo → upgrade
                    repo.upgradeAnonymousAccount(email, password)
                } else {
                    // Caso 2: registro normal
                    repo.register(email, password)
                }
                // Ahora que el usuario YA está creado, obtener su UID y su email
                val user = FirebaseAuth.getInstance().currentUser
                val finalEmail = user?.email ?: ""

                // Crear perfil inicial con nombre + email + fecha
                createInitialUserProfile(name, finalEmail)

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

            "operation not allowed" in msg ->
                R.string.error_anon_not_allowed

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

    fun loginAnonymously(onSuccess: () -> Unit) {
        _state.value = AuthUiState.Loading

        viewModelScope.launch {
            try {
                repo.loginAnonymously()

                // Si es anónimo, en Firestore creo un perfil inicial por defecto
                val anon = FirebaseAuth.getInstance().currentUser
                if (anon != null) {
                    val repoProfile = UserProfileRepository()
                    repoProfile.createInitialProfile(
                        name = "Invitado",
                        email = "",
                        createdAt = System.currentTimeMillis()
                    )
                }

                _state.value = AuthUiState.Success
                onSuccess()
            } catch (e: Exception) {
                _state.value = AuthUiState.Error(R.string.error_unknown)
            }
        }
    }

    fun saveUserName(name: String) {
        viewModelScope.launch {
            val repo = UserProfileRepository()
            repo.updateName(name)
        }
    }

    private suspend fun createInitialUserProfile(name: String, email: String) {
        val repo = UserProfileRepository()

        repo.createInitialProfile(
            name = name,
            email = email,
            createdAt = System.currentTimeMillis()
        )
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onResult: (Boolean, Int?) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser

        // Si no hay usuario → error.
        if (user == null) {
            onResult(false, R.string.error_unknown)
            return
        }

        val email = user.email
        if (email.isNullOrBlank()) {
            onResult(false, R.string.error_unknown)
            return
        }

        val credential = EmailAuthProvider.getCredential(email, currentPassword)

        viewModelScope.launch {
            try {
                // 1) Reautenticar usuario (necesario en FIrebase)
                user.reauthenticate(credential).await()

                // 2) Cambiar contraseña
                user.updatePassword(newPassword).await()

                onResult(true, null)

            } catch (e: Exception) {
                onResult(false, R.string.error_wrong_password)
            }
        }
    }

    private suspend fun deleteUserData(uid: String) {
        val db = FirebaseFirestore.getInstance()

        val userDoc = db.collection("usuarios").document(uid)

        val subcollections = listOf("tareas", "perfil", "preferences")

        // Borrar documentos dentro de cada subcolección
        for (sub in subcollections) {
            val col = userDoc.collection(sub).get().await()
            for (doc in col.documents) {
                doc.reference.delete().await()
            }
        }

        // Finalmente, borrar el documento principal del usuario
        userDoc.delete().await()
    }


    fun deleteAccount(onResult: (Boolean, Int?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser ?: run {
            onResult(false, R.string.error_unknown)
            return
        }

        viewModelScope.launch {
            try {
                val uid = user.uid

                // Si es anónimo no necesita reautenticación
                if (user.isAnonymous) {
                    deleteUserData(uid)
                    user.delete().await()
                    onResult(true, null)
                    return@launch
                }

                // Para usuarios registrados
                try {
                    user.delete().await()   // puede fallar por falta de reauth
                } catch (e: Exception) {

                    // DETECCIÓN REAL DE ERROR DE REAUTENTICACIÓN
                    val msg = e.message?.lowercase().orEmpty()

                    if (
                        "requires recent login" in msg ||
                        "recent authentication" in msg ||
                        e is FirebaseAuthRecentLoginRequiredException
                    ) {
                        onResult(false, R.string.error_reauth_required)
                        return@launch
                    }

                    throw e // otros errores
                }

                // Si delete ha funcionado borrar datos
                deleteUserData(uid)

                onResult(true, null)

            } catch (e: Exception) {
                onResult(false, R.string.error_unknown)
            }
        }
    }
}