package com.roro.storage.presentation

import com.roro.core.model.Folder

data class StorageUiState(
    val isLoading: Boolean = false,
    val folders: List<Folder> = emptyList(),
    val defaultFolders: List<String> = emptyList(),
    val errorMessage: String? = null
)
