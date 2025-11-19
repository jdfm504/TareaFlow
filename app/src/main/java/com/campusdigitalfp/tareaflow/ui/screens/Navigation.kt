package com.campusdigitalfp.tareaflow.ui.screens.login

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.campusdigitalfp.tareaflow.ui.screens.home.HomeScreen
import com.campusdigitalfp.tareaflow.ui.screens.login.LoginScreen
import com.campusdigitalfp.tareaflow.ui.screens.register.RegisterScreen

@Composable
fun TareaFlowNavHost() {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
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
                    navController.popBackStack() // vuelve al login
                }
            )
        }
        composable("home") {
            HomeScreen()
        }
    }
}