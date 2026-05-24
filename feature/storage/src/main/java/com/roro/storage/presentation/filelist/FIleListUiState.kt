package com.roro.storage.presentation.filelist

import com.roro.core.domain.model.VoiceNoteItem
import com.roro.core.model.Folder
import com.roro.core.model.VoiceNote
import java.util.UUID

val EMPTY_UUID: UUID = UUID(0L, 0L)

data class FileListUiState(
    val isLoading: Boolean = false,
    val item: List<VoiceNote> = emptyList(),
    val isSelectMode: Boolean = false,
    val selectedIds: Set<UUID> = emptySet(),
    val isMenuExpanded: Boolean = false,
    val isDialog: Boolean = false,
    val folderId: UUID = EMPTY_UUID,
    val folderName: String = "",
    val errorMessage: String? = null,

    // 폴더 리스트
    val folderList: List<Folder> = emptyList()

)

enum class FileListDialogType {
    REMOVE_SELECTED,    // 선택 항목 삭제
    NEW_FOLDER          // 새폴더
}