package com.campusdigitalfp.tareaflow.data.model

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val createdAt: Long = 0L   // timestamp en milisegundos
)