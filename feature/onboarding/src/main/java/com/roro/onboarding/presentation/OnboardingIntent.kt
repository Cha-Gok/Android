package com.roro.onboarding.presentation

import com.roro.core.datastore.Language

sealed interface OnboardingIntent {
    data object Initialize : OnboardingIntent
    data class SelectLanguage(val language: Language) : OnboardingIntent
    data class PagerChanged(val index: Int) : OnboardingIntent // 사용자가 직접 스와이프 시 상태 동기화
    data object ClickNext : OnboardingIntent // 다음 버튼
    data object ClickBack : OnboardingIntent // 이전 버튼 (추가)
    data object ClickSkip : OnboardingIntent // 건너뛰기
    data object ClickPermissionRequest : OnboardingIntent
    data class PermissionResult(val granted: Boolean) : OnboardingIntent
    data object ClickStart : OnboardingIntent
}

sealed interface OnboardingEffect {
    data object RequestAudioPermission : OnboardingEffect
    data object NavigationToMain : OnboardingEffect
    data class ScrollToPage(val index: Int) : OnboardingEffect
    data class ShowToast(val message: String) : OnboardingEffect
}