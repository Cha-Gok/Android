package com.roro.chagok.splash.domain

import com.roro.chagok.splash.datasource.SplashRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetOnboardingStatusUseCase @Inject constructor(
    private val repository: SplashRepository
) {
    operator fun invoke(): Flow<Boolean> {
        return repository.getOnboardingStatus()
    }
}