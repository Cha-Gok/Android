package com.roro.onboarding.presentation

import com.roro.core.datastore.Language

data class OnboardingUiState(
    val isLoading: Boolean = false,
    val currentPage: Int = 0,
    val selectedLanguage: Language = Language.KOREAN,
    val isPermissionGranted: Boolean = false,
    val errorMessage: String? = null,
    val modelDownloadState: ModelDownloadState = ModelDownloadState() // ← 추가
) {
    val isLastPage: Boolean
        get() = currentPage == 4 // 3 → 4

    val isStartEnabled: Boolean
        get() = !isLoading && isLastPage

    val showSkipButton: Boolean
        get() = currentPage < 2

    val isNextEnabled: Boolean // ← 추가: Page 3에서 다운 완료 전까지 버튼 막기
        get() = if (currentPage == 3) modelDownloadState.allDone else !isLoading

    val isDownloadStarted: Boolean
        get() = modelDownloadState.stt != DownloadItemState.Idle ||
                modelDownloadState.summarize != DownloadItemState.Idle ||
                modelDownloadState.translate != DownloadItemState.Idle
}

data class ModelDownloadState(
    val stt: DownloadItemState = DownloadItemState.Idle,
    val summarize: DownloadItemState = DownloadItemState.Idle,
    val translate: DownloadItemState = DownloadItemState.Idle,
) {
    val allDone: Boolean
        get() = listOf(stt, summarize, translate)
            .all { it == DownloadItemState.Done || it == DownloadItemState.Unavailable }
}

enum class DownloadItemState {
    Idle, Downloading, Done, Unavailable, Failed
}