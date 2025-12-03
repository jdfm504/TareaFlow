package com.campusdigitalfp.tareaflow.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusdigitalfp.tareaflow.data.model.Task
import com.campusdigitalfp.tareaflow.data.model.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class TaskViewModel : ViewModel() {

    private val repository = TaskRepository()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    var selected = mutableStateListOf<String>()
    var isActionMode by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            repository.listenToTasks().collect { taskList ->
                _tasks.value = taskList
            }
        }
    }

    fun addTask(title: String, description: String) {
        val newTask = Task(title = title, description = description)
        repository.addTask(newTask)
    }

    fun toggleSelection(taskId: String) {
        if (selected.contains(taskId)) selected.remove(taskId)
        else selected.add(taskId)
        isActionMode = selected.isNotEmpty()
    }

    fun clearSelection() {
        selected.clear()
        isActionMode = false
    }

    fun deleteSelected() {
        selected.forEach { repository.deleteTask(it) }
        clearSelection()
    }

    // Semilla temporal para ver lista
    //fun seed() {
    //   if (_tasks.isEmpty()) {
    //        addTask("Comprar pan", "Integral y sin sal")
    //        addTask("Estudiar DAM", "Compose + Firestore")
    //        addTask("Gimnasio", "Pierna + core")
    //    }
    // }
}