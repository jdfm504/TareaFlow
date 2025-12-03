package com.campusdigitalfp.tareaflow.data.model

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.campusdigitalfp.tareaflow.data.model.Task
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow

class TaskRepository {

    private val db = FirebaseFirestore.getInstance()
    private val tasksCollection = db.collection("tareas")

    // 🔹 Escuchar tareas en tiempo real
    fun listenToTasks(): Flow<List<Task>> = callbackFlow {
        val listener: ListenerRegistration = tasksCollection
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

    // 🔹 Agregar una nueva tarea
    fun addTask(task: Task) {
        val id = if (task.id.isBlank()) null else task.id
        val doc = if (id != null) tasksCollection.document(id) else tasksCollection.document()
        doc.set(task)
    }

    // 🔹 Eliminar una tarea
    fun deleteTask(taskId: String) {
        tasksCollection.document(taskId).delete()
    }
}
