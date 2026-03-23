package com.roro.storage.presentation

import com.roro.core.model.Folder
import java.util.UUID

sealed interface StorageIntent {
    data object Initialize : StorageIntent
    data class CreateDummyVoiceNote(val folderName: String) : StorageIntent
    data class CreateFolder(val folderName: String) : StorageIntent
    data class MoveToTrash(val folder: Folder) : StorageIntent
    data class MoveToTrashVoiceNotes(val ids: List<UUID>) : StorageIntent
    data class RestoreFromTrash(val folder: Folder) : StorageIntent
    object RefreshDefaults : StorageIntent
}

sealed interface StorageEffect {
    data class ShowToast(val message: String) : StorageEffect
    object ClearFolderInput : StorageEffect
}