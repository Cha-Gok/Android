package com.roro.onboarding.domain

import javax.inject.Inject

class SetOnboardingCompletedUseCase @Inject constructor(
    private val repository: OnBoardingRepository
) {
    suspend operator fun invoke() {
        repository.setOnboardingCompleted()
    }
}