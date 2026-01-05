package com.campusdigitalfp.tareaflow.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private var isNavigating = false
private val navScope = MainScope()

fun NavController.safeNavigate(route: String) {
    if (isNavigating) return

    isNavigating = true
    this.navigate(route)

    navScope.launch {
        delay(300)   // Previene doble click incluso en dropdowns
        isNavigating = false
    }
}

fun NavController.safeNavigate(
    route: String,
    builder: NavOptionsBuilder.() -> Unit
) {
    if (isNavigating) return

    isNavigating = true
    this.navigate(route, builder)

    navScope.launch {
        delay(300)
        isNavigating = false
    }
}