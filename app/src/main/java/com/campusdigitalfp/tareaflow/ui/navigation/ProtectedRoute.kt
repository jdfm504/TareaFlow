package com.campusdigitalfp.tareaflow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProtectedRoute(
    navController: NavController,
    content: @Composable () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    if (user != null) {
        // Usuario autenticado, mostramos el contenido
        content()
    } else {
        // Usuario no autenticado, vamos al login
        LaunchedEffect(Unit) {
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    }
}
