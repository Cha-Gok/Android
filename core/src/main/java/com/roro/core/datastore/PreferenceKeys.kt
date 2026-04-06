package com.roro.core.datastore

// 키 정의
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
}