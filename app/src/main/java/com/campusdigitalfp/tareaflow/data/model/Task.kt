package com.campusdigitalfp.tareaflow.data.model

// Elemento tarea con sus campos
data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val done: Boolean = false
)