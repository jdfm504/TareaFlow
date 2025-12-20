package com.campusdigitalfp.tareaflow.data

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val currentUser get() = auth.currentUser

    // Registro
    suspend fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    // Login
    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    // Logout
    fun logout() {
        auth.signOut()
    }

    // Login anónimo
    suspend fun loginAnonymously() {
        auth.signInAnonymously().await()
    }

    // Upgrade de anónimo a usuario registrado
    suspend fun upgradeAnonymousAccount(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        auth.currentUser!!.linkWithCredential(credential).await()
    }
}