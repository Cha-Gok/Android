package com.roro.onboarding.data.repository

import com.roro.core.data.datasource.DataStoreSource
import com.roro.onboarding.domain.OnBoardingRepository
import javax.inject.Inject

class OnBoardingRepositoryImpl @Inject constructor(
    private val dataStore: DataStoreSource
) : OnBoardingRepository {
    override suspend fun setOnboardingCompleted() {
        dataStore.setOnboardingCompleted()
    }
}