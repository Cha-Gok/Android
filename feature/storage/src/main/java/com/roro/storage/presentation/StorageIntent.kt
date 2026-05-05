package com.roro.storage.presentation

import com.roro.core.model.Folder
import com.roro.core.model.VoiceNote
import com.roro.storage.presentation.home.DefaultFolderType
import java.util.UUID

sealed interface StorageIntent {
    data object Initialize : StorageIntent

    // 4개 박스 클릭
    data class ClickFolderType(val type: DefaultFolderType) : StorageIntent

    // 파일목록 가져오기
    data class FetchVoiceNote(val folderId: UUID) : StorageIntent

    // 임시 인텐트
    data class CreateDummyVoiceNote(val folderName: String) : StorageIntent
    data class CreateFolder(val folderName: String) : StorageIntent
    data class MoveToTrash(val folder: Folder) : StorageIntent
    data class MoveToTrashVoiceNotes(val ids: List<UUID>) : StorageIntent
    data class RestoreFromTrash(val folder: Folder) : StorageIntent
    data class RestoreVoiceNote(val voiceNote: VoiceNote) : StorageIntent
    data class RemoveVoiceNote(val voiceNote: VoiceNote) : StorageIntent
    data class RemoveFolder(val folder: Folder) : StorageIntent
    data class RenameFolder(val folder: Folder) : StorageIntent
    data class RenameVoiceNote(val voiceNote: VoiceNote) : StorageIntent
    object RefreshDefaults : StorageIntent
    object SortByCreatedAt : StorageIntent
    object SortByUpdatedAt : StorageIntent

}

sealed interface StorageEffect {
    data class ShowToast(val message: String) : StorageEffect
    object ClearFolderInput : StorageEffect
}