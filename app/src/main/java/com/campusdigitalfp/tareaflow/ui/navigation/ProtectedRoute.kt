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

    // Si NO hay usuario redirige a login, corrige fallo de recomposición de Home
    LaunchedEffect(user) {
        if (user == null) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // Si hay usuario, mostrar contenido protegido
    if (user != null) {
        content()
    }
}
