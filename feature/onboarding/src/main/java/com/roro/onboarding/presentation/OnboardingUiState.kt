package com.roro.onboarding.presentation

import com.roro.core.datastore.Language

data class OnboardingUiState(
    val isLoading: Boolean = false,
    val currentPage: Int = 0,
    val selectedLanguage: Language = Language.KOREAN,
    val isPermissionGranted: Boolean = false,
    val errorMessage: String? = null
) {
    val isLastPage: Boolean
        get() = currentPage == 3

    val isStartEnabled: Boolean
        get() = !isLoading && isLastPage

    val showSkipButton: Boolean
        get() = currentPage < 2
}
