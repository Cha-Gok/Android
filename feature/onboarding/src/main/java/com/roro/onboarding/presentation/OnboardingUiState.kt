package com.roro.onboarding.presentation

import com.roro.core.datastore.Language

data class OnboardingUiState(
    val isLoading: Boolean = false,
    val currentPage: Int = 0,
    val selectedLanguage: Language = Language.KOREAN,
    val isPermissionGranted: Boolean = false,
    val errorMessage: String? = null,
    val modelDownloadState: ModelDownloadState = ModelDownloadState(), // ← 추가
    val isEnvironmentChecked: Boolean = false  // 환경 체크 완료 여부
) {
    val isLastPage: Boolean
        get() = currentPage == 5 // 3 → 4

    val isStartEnabled: Boolean
        get() = !isLoading && isLastPage

    val showSkipButton: Boolean
        get() = currentPage < 2

    // isNextEnabled에 페이지 3 조건 추가
    val isNextEnabled: Boolean
        get() = when {
            currentPage == 3 -> isEnvironmentChecked && !modelDownloadState.isChecking
            currentPage == 4 && modelDownloadState.gemma == DownloadItemState.Downloading -> false
            currentPage == 4 && !isDownloadStarted -> true  // 다운로드 버튼 활성
            currentPage == 4 && isDownloadStarted -> modelDownloadState.allDone  // 완료 시 다음 활성
            else -> !isLoading
        }

    val isDownloadStarted: Boolean
        get() = modelDownloadState.gemma != DownloadItemState.Idle
}

data class ModelDownloadState(
    val isChecking: Boolean = false,
    val gemma: DownloadItemState = DownloadItemState.Idle,
    val progress: Float = 0f  // ← 추가
) {
    val allDone: Boolean
        get() = !isChecking && (gemma == DownloadItemState.Done || gemma == DownloadItemState.Unavailable)
}

//data class ModelDownloadState(
//    val isChecking: Boolean = true,           // ← 추가: 환경 확인 중
//    val stt: DownloadItemState = DownloadItemState.Idle,
//    val summarize: DownloadItemState = DownloadItemState.Idle,
//    val translate: DownloadItemState = DownloadItemState.Idle,
//) {
//    val allDone: Boolean
//        get() = !isChecking && listOf(stt, summarize, translate)
//            .all { it == DownloadItemState.Done || it == DownloadItemState.Unavailable }
//}





enum class DownloadItemState {
    Idle, Checking, Downloading, Done, Unavailable, Failed, Required  // ← Required 추가
}