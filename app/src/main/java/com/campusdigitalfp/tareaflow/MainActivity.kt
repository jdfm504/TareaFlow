package com.campusdigitalfp.tareaflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.campusdigitalfp.tareaflow.ui.screens.login.TareaFlowNavHost
import com.campusdigitalfp.tareaflow.ui.theme.TareaFlowTheme
import com.campusdigitalfp.tareaflow.viewmodel.PreferencesViewModel
import com.campusdigitalfp.tareaflow.ui.screens.loading.LoadingScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Cargar preferencias de Firestore
            val prefsViewModel: PreferencesViewModel = viewModel()
            val prefs by prefsViewModel.prefs.collectAsState()

            val isLoaded by prefsViewModel.isLoaded.collectAsState()

            // Mostrar pantalla de carga hasta tener las preferencias
            if (!isLoaded) {
                LoadingScreen()
                return@setContent
            }

            // Aplicar tema visual desde Firestore
            TareaFlowTheme(darkTheme = prefs.darkTheme) {

                Surface {
                    val navController = rememberNavController()
                    TareaFlowNavHost(navController)
                }
            }
        }
    }
}