package com.campusdigitalfp.tareaflow.data

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val currentUser get() = auth.currentUser

    suspend fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun loginAnonymously() {
        FirebaseAuth.getInstance().signInAnonymously().await()
    }

    suspend fun upgradeAnonymousAccount(email: String, password: String) {
        val credential = EmailAuthProvider.getCredential(email, password)
        FirebaseAuth.getInstance().currentUser!!.linkWithCredential(credential).await()
    }

}