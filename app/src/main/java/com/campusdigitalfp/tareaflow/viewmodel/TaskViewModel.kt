package com.campusdigitalfp.tareaflow.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.campusdigitalfp.tareaflow.data.model.Task
import java.util.UUID

class TaskViewModel : ViewModel() {

    private val _tasks = mutableStateListOf<Task>()
    val tasks: List<Task> get() = _tasks

    var isActionMode by mutableStateOf(false)
        private set

    private val _selected = mutableStateListOf<String>()
    val selected: List<String> get() = _selected

    fun addTask(title: String, description: String) {
        val id = UUID.randomUUID().toString()
        _tasks.add(Task(id = id, title = title, description = description))
    }

    fun toggleSelection(taskId: String) {
        if (_selected.contains(taskId)) _selected.remove(taskId) else _selected.add(taskId)
        isActionMode = _selected.isNotEmpty()
    }

    fun clearSelection() {
        _selected.clear()
        isActionMode = false
    }

    fun deleteSelected() {
        _tasks.removeAll { it.id in _selected }
        clearSelection()
    }

    // Semilla temporal para ver lista
    fun seed() {
        if (_tasks.isEmpty()) {
            addTask("Comprar pan", "Integral y sin sal")
            addTask("Estudiar DAM", "Compose + Firestore")
            addTask("Gimnasio", "Pierna + core")
        }
    }
}