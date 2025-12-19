package com.campusdigitalfp.tareaflow.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.campusdigitalfp.tareaflow.data.model.UserPreferences
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

    suspend fun loadPreferences(): UserPreferences {
        val doc = preferencesDocument() ?: return UserPreferences()

        val snapshot = doc.get().await()
        if (!snapshot.exists()) {
            // crear documento por primera vez
            doc.set(UserPreferences()).await()
            return UserPreferences()
        }

        return snapshot.toObject(UserPreferences::class.java) ?: UserPreferences()
    }

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