package com.roro.storage.presentation

import com.roro.core.model.Folder
import com.roro.core.model.VoiceNote
import java.util.UUID

data class StorageUiState(
    val selectedFolderType: DefaultFolderType = DefaultFolderType.DEFAULT,
    val isLoading: Boolean = false,
    // 현재 화면에 보여줄 리스트
    val voiceNote: List<VoiceNote> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val defaultFolders: List<String> = emptyList(),
    val errorMessage: String? = null
)
