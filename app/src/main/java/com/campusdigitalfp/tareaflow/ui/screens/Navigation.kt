package com.campusdigitalfp.tareaflow.ui.screens.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.campusdigitalfp.tareaflow.ui.navigation.ProtectedRoute
import com.campusdigitalfp.tareaflow.ui.screens.about.AboutScreen
import com.campusdigitalfp.tareaflow.ui.screens.home.HomeScreen
import com.campusdigitalfp.tareaflow.ui.screens.register.RegisterScreen
import com.google.firebase.auth.FirebaseAuth
import com.campusdigitalfp.tareaflow.ui.screens.home.TaskEditScreen
import com.campusdigitalfp.tareaflow.viewmodel.AuthViewModel
import com.campusdigitalfp.tareaflow.viewmodel.TaskViewModel
import com.campusdigitalfp.tareaflow.ui.screens.settings.SettingsScreen
import com.campusdigitalfp.tareaflow.viewmodel.PreferencesViewModel
import kotlinx.coroutines.tasks.await
import com.campusdigitalfp.tareaflow.viewmodel.UserProfileViewModel

@Composable
fun TareaFlowNavHost(
    navController: NavHostController,
    taskViewModel: TaskViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    profileViewModel: UserProfileViewModel
) {
    val auth = FirebaseAuth.getInstance()
    val prefsViewModel: PreferencesViewModel = viewModel()

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

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ============ ONBOARDING ============
        composable("onboarding") {
            OnboardingScreen(
                onStart = { navController.navigate("register") },
                onGoToLogin = { navController.navigate("login") }
            )
        }

        // ============ LOGIN ============
        composable("login") {
            val prefsViewModel: PreferencesViewModel = viewModel()

            LoginScreen(
                onLoginSuccess = {
                    profileViewModel.reload()
                    prefsViewModel.reload()
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onGoToRegister = { navController.navigate("register") }
            )
        }

        // ============ REGISTER ============
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    profileViewModel.reload()
                    prefsViewModel.reload()
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onGoToLogin = { navController.popBackStack() }
            )
        }

        // ============ HOME ============
        composable("home") {
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: "no-user"
            val taskViewModel: TaskViewModel = viewModel(key = "tasks_$currentUid")

            ProtectedRoute(navController) {
                HomeScreen(
                    navController = navController,
                    viewModel = taskViewModel,
                    authViewModel = authViewModel,
                    profileViewModel = profileViewModel
                )
            }
        }

        // ============ NUEVA TAREA ============
        composable("task/new") {
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: "no-user"
            val taskViewModel: TaskViewModel = viewModel(key = "tasks_$currentUid")

            TaskEditScreen(
                navController = navController,
                viewModel = taskViewModel
            )
        }

        // ============ EDITAR TAREA ============
        composable("task/{taskId}") { backStackEntry ->
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: "no-user"
            val taskViewModel: TaskViewModel = viewModel(key = "tasks_$currentUid")
            val taskId = backStackEntry.arguments?.getString("taskId")

            TaskEditScreen(
                navController = navController,
                viewModel = taskViewModel,
                taskId = taskId)
        }

        // ============ ABOUT ============
        composable("about") {
            AboutScreen(navController)
        }

        // ============ SETTINGS ============
        composable("settings") {
            SettingsScreen(
                navController = navController,
                profileViewModel = profileViewModel
            )
        }
    }
}
