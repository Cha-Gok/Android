package com.roro.chagok.splash.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.roro.chagok.splash.presentation.SplashScreen
import com.roro.core.navigation.Routes

fun NavGraphBuilder.splashGraph(
    onNavigateToMain: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
) {
    composable(Routes.SPLASH) {
        SplashScreen(
            onNavigateToMain = onNavigateToMain,
            onNavigateToOnboarding = onNavigateToOnboarding,
        )
    }
}