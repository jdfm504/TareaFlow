package com.campusdigitalfp.tareaflow.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.campusdigitalfp.tareaflow.data.model.UserProfile
import kotlinx.coroutines.tasks.await

class UserProfileRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private fun profileDocument() =
        auth.currentUser?.let { user ->
            db.collection("usuarios")
                .document(user.uid)
                .collection("perfil")
                .document("app")
        }

    suspend fun loadUserProfile(): UserProfile {
        val doc = profileDocument() ?: return UserProfile()

        val snapshot = doc.get().await()
        return if (snapshot.exists()) {
            snapshot.toObject(UserProfile::class.java) ?: UserProfile()
        } else {
            val user = auth.currentUser
            val newProfile = UserProfile(
                name = user?.displayName ?: "",
                email = user?.email ?: "",
                createdAt = System.currentTimeMillis()
            )

            doc.set(newProfile).await()
            newProfile
        }
    }

    suspend fun updateName(newName: String) {
        profileDocument()?.update("name", newName)?.await()
    }
}
