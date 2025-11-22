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

@Composable
fun TareaFlowNavHost(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val startDestination = if (auth.currentUser == null) "login" else "home"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onGoToRegister = {
                    navController.navigate("register")
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onGoToLogin = {
                    navController.popBackStack()
                }
            )
        }
        composable("home") {
            ProtectedRoute(navController) {
                HomeScreen(navController)
            }
        }
    }
}
