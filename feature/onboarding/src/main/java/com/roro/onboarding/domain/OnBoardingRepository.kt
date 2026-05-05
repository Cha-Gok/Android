package com.roro.onboarding.domain

interface OnBoardingRepository {
    // 온보딩 완료
    suspend fun setOnboardingCompleted()
}