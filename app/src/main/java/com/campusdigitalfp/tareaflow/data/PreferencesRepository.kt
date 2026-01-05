package com.campusdigitalfp.tareaflow.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.campusdigitalfp.tareaflow.data.model.UserPreferences
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PreferencesRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun preferencesDocument(): DocumentReference {
        val user = auth.currentUser
            ?: throw IllegalStateException("User not authenticated")

        return db.collection("usuarios")
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
        val doc = preferencesDocument()
        val snapshot = doc.get().await()

        if (!snapshot.exists()) {
            doc.set(UserPreferences(), SetOptions.merge()).await()
            return
        }

        // Fusionar campos por si faltan (por ejemplo darkTheme)
        val existing = snapshot.toObject(UserPreferences::class.java) ?: UserPreferences()
        doc.set(existing, SetOptions.merge()).await()
    }

    // ---------------------------
    // UPDATE
    // ---------------------------
    suspend fun updateDarkTheme(value: Boolean) {
        preferencesDocument()
            .set(mapOf("darkTheme" to value), SetOptions.merge())
            .await()
    }

    suspend fun updatePomodoro(minutes: Int) {
        preferencesDocument()
            .set(mapOf("pomodoroMinutes" to minutes), SetOptions.merge())
            .await()
    }

    suspend fun updateShortBreak(value: Int) {
        preferencesDocument()
            .set(mapOf("shortBreakMinutes" to value), SetOptions.merge())
            .await()
    }

    suspend fun updateLongBreak(value: Int) {
        preferencesDocument()
            .set(mapOf("longBreakMinutes" to value), SetOptions.merge())
            .await()
    }

    suspend fun updateCycles(value: Int) {
        preferencesDocument()
            .set(mapOf("cyclesUntilLongBreak" to value), SetOptions.merge())
            .await()
    }

    suspend fun updateAutoStart(value: Boolean) {
        preferencesDocument()
            .set(mapOf("autoStartNextPhase" to value), SetOptions.merge())
            .await()
    }

    suspend fun updatePhaseTipShown() {
        preferencesDocument()
            .set(mapOf("phaseTipShown" to true), SetOptions.merge())
            .await()
    }

    suspend fun updateSoundEnabled(value: Boolean) {
        preferencesDocument()
            .set(mapOf("soundEnabled" to value), SetOptions.merge())
            .await()
    }
}