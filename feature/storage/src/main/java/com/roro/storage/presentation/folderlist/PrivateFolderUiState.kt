package com.roro.storage.presentation.folderlist

import com.roro.core.domain.model.FolderItem
import com.roro.storage.presentation.home.FolderDialogType

data class PrivateFolderUiState(
    val isLoading: Boolean = false,
    val folderList: List<FolderItem> = emptyList(),
    val swipeFolder: FolderItem? = null,
    val isDialog: Boolean = false,
    val errorMessage: String? = null,
    val inputFolderName: String = "",
    val dialogType: FolderDialogType? = null,
    val selectedFolder: FolderItem? = null
)
