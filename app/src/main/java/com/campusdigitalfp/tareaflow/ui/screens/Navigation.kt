package com.campusdigitalfp.tareaflow.ui.screens.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.campusdigitalfp.tareaflow.ui.navigation.ProtectedRoute
import com.campusdigitalfp.tareaflow.ui.navigation.safeNavigate
import com.campusdigitalfp.tareaflow.ui.screens.about.AboutScreen
import com.campusdigitalfp.tareaflow.ui.screens.home.HomeScreen
import com.campusdigitalfp.tareaflow.ui.screens.register.RegisterScreen
import com.google.firebase.auth.FirebaseAuth
import com.campusdigitalfp.tareaflow.ui.screens.home.TaskEditScreen
import com.campusdigitalfp.tareaflow.ui.screens.pomodoro.PomodoroScreen
import com.campusdigitalfp.tareaflow.viewmodel.AuthViewModel
import com.campusdigitalfp.tareaflow.viewmodel.TaskViewModel
import com.campusdigitalfp.tareaflow.ui.screens.settings.SettingsScreen
import com.campusdigitalfp.tareaflow.viewmodel.PreferencesViewModel
import kotlinx.coroutines.tasks.await
import com.campusdigitalfp.tareaflow.viewmodel.UserProfileViewModel

@Composable
fun TareaFlowNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    profileViewModel: UserProfileViewModel,
    prefsViewModel: PreferencesViewModel
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

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ============ ONBOARDING ============
        composable("onboarding") {
            OnboardingScreen(
                onStart = { navController.safeNavigate("register") },
                onGoToLogin = { navController.safeNavigate("login") }
            )
        }

        // ============ LOGIN ============
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    profileViewModel.reload()
                    prefsViewModel.reload()
                    navController.safeNavigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onGoToRegister = { navController.safeNavigate("register") }
            )
        }

        // ============ REGISTER ============
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    profileViewModel.reload()
                    prefsViewModel.reload()
                    navController.safeNavigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onGoToLogin = { navController.safeNavigate("login") }
            )
        }

        // ============ HOME ============
        composable("home") {
            val taskViewModel: TaskViewModel = viewModel()

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
            val taskViewModel: TaskViewModel = viewModel()
            TaskEditScreen(navController, taskViewModel)
        }

        // ============ EDITAR TAREA ============
        composable("task/{taskId}") { backStackEntry ->
            val taskViewModel: TaskViewModel = viewModel()
            val taskId = backStackEntry.arguments?.getString("taskId")
            TaskEditScreen(navController, taskViewModel, taskId)
        }

        // ============ ABOUT ============
        composable("about") {
            AboutScreen(navController)
        }

        // ============ SETTINGS ============
        composable("settings") {
            SettingsScreen(
                navController = navController,
                profileViewModel = profileViewModel,
                prefsViewModel = prefsViewModel
            )
        }

        // ============ POMODORO ============
        composable("pomodoro") {
            PomodoroScreen(navController = navController, prefsViewModel = prefsViewModel)
        }
    }
}
