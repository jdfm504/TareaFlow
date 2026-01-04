package com.campusdigitalfp.tareaflow.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusdigitalfp.tareaflow.data.TaskRepository
import com.campusdigitalfp.tareaflow.data.model.Task
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {

    private val repository = TaskRepository()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    var selected = mutableStateListOf<String>()
    var isActionMode by mutableStateOf(false)
        private set
    private var listening = false
    private var tasksJob: Job? = null

    var showPending by mutableStateOf(true)
        private set

    var showCompleted by mutableStateOf(false)
        private set

    init {
        startListeningToTasks()
    }

    // --------------------------------------------------------------
    // Debe llamarse desde LoginScreen/Navigation
    // cuando el usuario está logueado
    // --------------------------------------------------------------
    fun startListeningToTasks() {
        tasksJob?.cancel()
        listening = true

        tasksJob = viewModelScope.launch {
            repository.listenToTasks().collect { taskList ->
                _tasks.value = taskList
            }
        }
    }

    // --------------------------------------------------------------
    // detener escucha (para logout / cambio de usuario)
    // --------------------------------------------------------------
    fun stopListeningToTasks() {
        tasksJob?.cancel()
        tasksJob = null
        listening = false

        // Limpiamos estado para no ver datos del usuario anterior
        _tasks.value = emptyList()
        clearSelection()
    }

    // --------------------------------------------------------------
    // CRUD
    // --------------------------------------------------------------
    fun addTask(title: String, description: String) {
        viewModelScope.launch {
            val newTask = Task(title = title, description = description)
            repository.addTask(newTask)
        }
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
        viewModelScope.launch {
            selected.forEach { repository.deleteTask(it) }
            clearSelection()
        }
    }

    fun toggleDone(taskId: String) {
        viewModelScope.launch {
            val task = _tasks.value.find { it.id == taskId } ?: return@launch
            val updatedTask = task.copy(done = !task.done)
            repository.updateTask(updatedTask)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun togglePending() {
        showPending = !showPending
    }

    fun toggleCompleted() {
        showCompleted = !showCompleted
    }
}