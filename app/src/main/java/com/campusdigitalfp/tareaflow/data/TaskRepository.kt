package com.campusdigitalfp.tareaflow.data

import com.campusdigitalfp.tareaflow.data.model.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class TaskRepository {

    private val db = FirebaseFirestore.getInstance()
    private val tasksCollection = db.collection("tareas")

    // Escuchar tareas en tiempo real
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

    // Agregar una nueva tarea
    fun addTask(task: Task) {
        val doc = tasksCollection.document()         // crea el doc con ID generado
        val taskWithId = task.copy(id = doc.id)      // copia el ID dentro del objeto
        doc.set(taskWithId)                          // guarda el objeto con su id dentro
    }

    // Eliminar una tarea
    fun deleteTask(taskId: String) {
        tasksCollection.document(taskId).delete()
    }

    // Editar una tarea
    fun updateTask(task: Task) {
        if (task.id.isNotBlank()) {
            tasksCollection.document(task.id).set(task)
        }
    }
}