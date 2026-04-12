package com.roro.chagok.splash.presentation

sealed interface SplashIntent {
    data object CheckOnboardingStatus : SplashIntent
}

sealed interface SplashEffect {
    data object NavigationToMain : SplashEffect
    data object NavigationToOnboarding : SplashEffect
}