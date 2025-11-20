package com.campusdigitalfp.tareaflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.campusdigitalfp.tareaflow.ui.screens.login.TareaFlowNavHost
import com.campusdigitalfp.tareaflow.ui.theme.TareaFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TareaFlowTheme {
                Surface {
                    val navController = rememberNavController()
                    TareaFlowNavHost(navController)
                }
            }
        }
    }
}