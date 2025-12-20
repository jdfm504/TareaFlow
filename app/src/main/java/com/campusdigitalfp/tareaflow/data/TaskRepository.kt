package com.campusdigitalfp.tareaflow.data

import com.campusdigitalfp.tareaflow.data.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TaskRepository {

    // Instancia de la BBDD de Firestore
    private val db = FirebaseFirestore.getInstance()
    // Instancia de Authentication
    private val auth = FirebaseAuth.getInstance()
    // ----------------------------------------------------
    // Colección del usuario logueado:
    // /usuarios/{uid}/tareas
    // ----------------------------------------------------
    private fun getUserTasksCollection() = db
        .collection("usuarios")
        .document(getUidOrThrow())
        .collection("tareas")

    // obtener UID o lanzar error si no hay usuario
    private fun getUidOrThrow(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("ERROR: No hay usuario logueado.")
    }

    // Escuchar tareas en tiempo real
    fun listenToTasks(): Flow<List<Task>> = callbackFlow {
        val listener: ListenerRegistration = getUserTasksCollection()
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Task::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(tasks)
            }
        awaitClose { listener.remove() }
    }

    // Agregar una nueva tarea
    suspend fun addTask(task: Task) {
        val doc = getUserTasksCollection().document() // crea el doc con ID de usuario
        val taskWithId = task.copy(id = doc.id)      // copia el ID dentro del objeto
        doc.set(taskWithId).await()                  // guarda el objeto con su id dentro
    }

    // Eliminar una tarea
    suspend fun deleteTask(taskId: String) {
        getUserTasksCollection().document(taskId).delete().await()
    }

    // Editar una tarea
    suspend fun updateTask(task: Task) {
        if (task.id.isNotBlank()) {
            getUserTasksCollection()
                .document(task.id)
                .set(task)
                .await()
        }
    }
}