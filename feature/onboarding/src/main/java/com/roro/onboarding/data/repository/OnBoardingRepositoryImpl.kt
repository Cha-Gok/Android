package com.roro.onboarding.data.repository

import com.roro.core.datastore.Language
import com.roro.onboarding.data.datasource.DataStoreSource
import com.roro.onboarding.domain.OnBoardingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OnBoardingRepositoryImpl @Inject constructor(
    private val dataStore: DataStoreSource
) : OnBoardingRepository {
    override suspend fun setSelectedLanguage(language: Language) {
        dataStore.setLanguage(language)
    }

    override fun getSelectedLanguage(): Flow<Language> {
        return dataStore.getLanguage()
    }

    override suspend fun setOnboardingCompleted() {
        dataStore.setOnboardingCompleted()
    }
}