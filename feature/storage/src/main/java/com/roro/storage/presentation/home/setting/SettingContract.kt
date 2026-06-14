package com.roro.storage.presentation.home.setting

import com.roro.core.datastore.Language

sealed interface SettingIntent {
    data object LoadData : SettingIntent
    data class SelectLanguage(val language: Language) : SettingIntent
    data object ConfirmLanguage : SettingIntent
    data object DeleteGemmaModel : SettingIntent
    data object NavigateBack : SettingIntent
}

data class SettingUiState(
    val selectedLanguage: Language = Language.KOREAN,
    val isModelDownloaded: Boolean = false,
    val isDeleteConfirmVisible: Boolean = false,
)

sealed interface SettingEffect {
    data object NavigateBack : SettingEffect
    data class ShowToast(val message: String) : SettingEffect
}