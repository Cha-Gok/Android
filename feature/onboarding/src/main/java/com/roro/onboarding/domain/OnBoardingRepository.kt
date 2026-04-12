package com.roro.onboarding.domain

import com.roro.core.datastore.Language
import kotlinx.coroutines.flow.Flow

interface OnBoardingRepository {
    // 언어선택
    suspend fun setSelectedLanguage(language: Language)

    // 언어조회
    fun getSelectedLanguage(): Flow<Language>

    // 온보딩 완료
    suspend fun setOnboardingCompleted()
}