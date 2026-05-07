package com.roro.storage.presentation.home

import com.roro.core.datastore.Language
import com.roro.core.model.Folder
import com.roro.core.model.VoiceNote

/**
 * 홈화면 uiState 정의
 *
 * @author sehoon
 * @since 2026. 5. 5.
 * @modified
 */
data class StorageUiState(
    val selectedFolderType: DefaultFolderType = DefaultFolderType.DEFAULT,
    val isLoading: Boolean = false,
    val isDialog: Boolean = false,
    val selectedLanguage: Language = Language.KOREAN, // dataStore 연결
    // 현재 화면에 보여줄 리스트
    val voiceNote: List<VoiceNote> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val defaultFolders: List<String> = emptyList(),
    val errorMessage: String? = null
)