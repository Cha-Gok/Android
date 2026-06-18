package com.roro.onboarding.presentation

import com.roro.core.datastore.Language

data class OnboardingUiState(
    val isLoading: Boolean = false,
    val currentPage: Int = 0,
    val selectedLanguage: Language = Language.KOREAN,
    val isPermissionGranted: Boolean = false,
    val errorMessage: String? = null,
    val modelDownloadState: ModelDownloadState = ModelDownloadState(),
    val isEnvironmentChecked: Boolean = false,
    val isCheckingEnvironment: Boolean = false,
    val setupStep: SetupStep = SetupStep.CHECKING
) {
    val isLastPage: Boolean
        get() = currentPage == 4

    val isStartEnabled: Boolean
        get() = !isLoading && isLastPage

    val showSkipButton: Boolean
        get() = currentPage < 2

    val isNextEnabled: Boolean
        get() = when {
            currentPage == 3 && isCheckingEnvironment -> false

            currentPage == 3 &&
                    modelDownloadState.gemma == DownloadItemState.Required ->
                true

            currentPage == 3 &&
                    modelDownloadState.gemma == DownloadItemState.Done ->
                true

            currentPage == 3 &&
                    modelDownloadState.gemma == DownloadItemState.Downloading ->
                false

            else -> !isLoading
        }

    val isDownloadStarted: Boolean
        get() = modelDownloadState.gemma == DownloadItemState.Downloading
                || modelDownloadState.gemma == DownloadItemState.Done
                || modelDownloadState.gemma == DownloadItemState.Failed
}

data class ModelDownloadState(
    val isChecking: Boolean = false,
    val gemma: DownloadItemState = DownloadItemState.Idle,
    val progress: Float = 0f
) {
    val allDone: Boolean
        get() = !isChecking && (gemma == DownloadItemState.Done || gemma == DownloadItemState.Unavailable)
}

enum class DownloadItemState {
    Idle, Checking, Downloading, Done, Unavailable, Failed, Required
}