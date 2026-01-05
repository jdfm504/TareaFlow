package com.campusdigitalfp.tareaflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.campusdigitalfp.tareaflow.ui.screens.login.TareaFlowNavHost
import com.campusdigitalfp.tareaflow.ui.theme.TareaFlowTheme
import com.campusdigitalfp.tareaflow.viewmodel.PreferencesViewModel
import com.campusdigitalfp.tareaflow.ui.screens.loading.LoadingScreen
import com.campusdigitalfp.tareaflow.viewmodel.UserProfileViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val prefsViewModel: PreferencesViewModel = viewModel()
            val profileViewModel: UserProfileViewModel = viewModel()

            val prefs by prefsViewModel.prefs.collectAsState()
            val isLoaded by prefsViewModel.isLoaded.collectAsState()
            val navController = rememberNavController()

            // Mostrar pantalla de carga hasta tener las preferencias
            if (!isLoaded) {
                LoadingScreen()
                return@setContent
            }

            // Aplicar tema visual desde Firestore
            TareaFlowTheme(darkTheme = prefs.darkTheme) {
                Surface {

                    TareaFlowNavHost(
                        navController = navController,
                        authViewModel = viewModel(),
                        profileViewModel = profileViewModel,
                        prefsViewModel = prefsViewModel
                    )
                }
            }
        }
    }
}