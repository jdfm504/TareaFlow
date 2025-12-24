package com.campusdigitalfp.tareaflow.data.model

// Elemento usuario con sus campos
data class UserProfile(
    val name: String = "",
    val email: String = "",
    val createdAt: Long = 0L   // timestamp en milisegundos
)