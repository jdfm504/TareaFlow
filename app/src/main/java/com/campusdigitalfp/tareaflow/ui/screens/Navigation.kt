package com.campusdigitalfp.tareaflow.ui.screens.login

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.campusdigitalfp.tareaflow.ui.navigation.ProtectedRoute
import com.campusdigitalfp.tareaflow.ui.screens.home.HomeScreen
import com.campusdigitalfp.tareaflow.ui.screens.login.LoginScreen
import com.campusdigitalfp.tareaflow.ui.screens.register.RegisterScreen
import com.google.firebase.auth.FirebaseAuth
import com.campusdigitalfp.tareaflow.ui.screens.home.TaskEditScreen
import com.campusdigitalfp.tareaflow.ui.screens.login.OnboardingScreen

@Composable
fun TareaFlowNavHost(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    // Si no hay usuario: onboarding. Si lo hay: Home.
    val startDestination = if (auth.currentUser == null) "onboarding" else "home"

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
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onGoToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onGoToLogin = { navController.popBackStack() }
            )
        }

        composable("home") {
            ProtectedRoute(navController) {
                HomeScreen(navController)
            }
        }

        composable("task/new") {
            TaskEditScreen(navController = navController)
        }

        composable("task/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            TaskEditScreen(navController = navController, taskId = taskId)
        }
    }
}
