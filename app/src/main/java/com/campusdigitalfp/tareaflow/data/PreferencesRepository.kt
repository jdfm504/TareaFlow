package com.campusdigitalfp.tareaflow.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.campusdigitalfp.tareaflow.data.model.UserPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PreferencesRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun preferencesDocument() =
        auth.currentUser?.let { user ->
            db.collection("usuarios")
                .document(user.uid)
                .collection("preferences")
                .document("app")
        }

    // ---------------------------
    // LISTENER EN TIEMPO REAL
    // ---------------------------
    fun listenPreferences(): Flow<UserPreferences> = callbackFlow {
        val docRef = preferencesDocument()

        if (docRef == null) {
            trySend(UserPreferences())
            close()
            return@callbackFlow
        }

        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(UserPreferences())
                return@addSnapshotListener
            }

            val prefs = snapshot?.toObject(UserPreferences::class.java) ?: UserPreferences()
            trySend(prefs)
        }

        awaitClose { registration.remove() }
    }

    // ---------------------------
    // INICIAL
    // ---------------------------
    suspend fun ensureDocumentExists() {
        val doc = preferencesDocument() ?: return
        val snapshot = doc.get().await()
        if (!snapshot.exists()) {
            doc.set(UserPreferences()).await()
        }
    }

    // ---------------------------
    // UPDATE
    // ---------------------------
    suspend fun updateDarkTheme(value: Boolean) {
        preferencesDocument()?.update("darkTheme", value)?.await()
    }

    suspend fun updateLanguage(lang: String) {
        preferencesDocument()?.update("language", lang)?.await()
    }

    suspend fun updatePomodoro(minutes: Int) {
        preferencesDocument()?.update("pomodoroMinutes", minutes)?.await()
    }
}