package com.roro.core.datastore

// 읽기/쓰기 로직
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val onboardingCompletedFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
    }

    val selectedLanguageFlow: Flow<Language> = dataStore.data.map { preferences ->
        Language.from(preferences[PreferencesKeys.SELECTED_LANGUAGE] ?: "ko")
    }

    suspend fun setOnboardingCompleted(isCompleted: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = isCompleted
        }
    }

    suspend fun setSelectedLanguage(language: Language) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_LANGUAGE] = language.code
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}