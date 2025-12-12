package com.campusdigitalfp.tareaflow.ui.screens.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.campusdigitalfp.tareaflow.ui.navigation.ProtectedRoute
import com.campusdigitalfp.tareaflow.ui.screens.about.AboutScreen
import com.campusdigitalfp.tareaflow.ui.screens.home.HomeScreen
import com.campusdigitalfp.tareaflow.ui.screens.login.LoginScreen
import com.campusdigitalfp.tareaflow.ui.screens.register.RegisterScreen
import com.google.firebase.auth.FirebaseAuth
import com.campusdigitalfp.tareaflow.ui.screens.home.TaskEditScreen
import com.campusdigitalfp.tareaflow.ui.screens.login.OnboardingScreen
import com.campusdigitalfp.tareaflow.viewmodel.TaskViewModel
import kotlinx.coroutines.tasks.await

@Composable
fun TareaFlowNavHost(
    navController: NavHostController,
    taskViewModel: TaskViewModel = viewModel()
) {
    val auth = FirebaseAuth.getInstance()

    // Validar usuario al arrancar la app
    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            try {
                // Forzar sincronización con Firebase
                user.reload().await()
            } catch (e: Exception) {
                // Si Firebase dice que el usuario ya no existe → cerrar sesión
                auth.signOut()
            }
        }
    }

    // Si no hay usuario: onboarding. Si lo hay: Home.
    val startDestination = if (auth.currentUser == null) "onboarding" else "home"

    // Si ya está logueado, iniciar escucha de tareas
    if (auth.currentUser != null) {
        taskViewModel.startListeningToTasks()
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable("onboarding") {
            OnboardingScreen(
                onStart = { navController.navigate("register") },
                onGoToLogin = { navController.navigate("login") }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    taskViewModel.startListeningToTasks()
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onGoToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    taskViewModel.startListeningToTasks()
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onGoToLogin = { navController.popBackStack() }
            )
        }

        composable("home") {
            ProtectedRoute(navController) {
                // pasamos el mismo TaskViewModel
                HomeScreen(
                    navController = navController,
                    viewModel = taskViewModel
                )
            }
        }

        composable("task/new") {
            TaskEditScreen(navController = navController)
        }

        composable("task/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            TaskEditScreen(navController = navController, taskId = taskId)
        }

        composable("about") {
            AboutScreen(navController)
        }

    }
}
