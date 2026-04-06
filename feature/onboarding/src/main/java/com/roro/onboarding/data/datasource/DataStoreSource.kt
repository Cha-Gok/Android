package com.roro.onboarding.data.datasource

import com.roro.core.datastore.AppDataStore
import com.roro.core.datastore.Language
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DataStoreSource @Inject constructor(
    private val appDataStore: AppDataStore
) {
    suspend fun setOnboardingCompleted() {
        appDataStore.setOnboardingCompleted(true)
    }

    suspend fun setLanguage(language: Language) {
        appDataStore.setSelectedLanguage(language)
    }

    fun getLanguage(): Flow<Language> {
        return appDataStore.selectedLanguageFlow
    }
}
