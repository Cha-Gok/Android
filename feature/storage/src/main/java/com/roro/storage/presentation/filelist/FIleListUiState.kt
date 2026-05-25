package com.roro.storage.presentation.filelist

import com.roro.core.domain.model.FolderItem
import com.roro.core.domain.model.VoiceNoteItem
import com.roro.core.model.VoiceNote
import java.util.UUID

val EMPTY_UUID: UUID = UUID(0L, 0L)

data class FileListUiState(
    val isLoading: Boolean = false,
    val item: List<VoiceNoteItem> = emptyList(),
    val isSelectMode: Boolean = false,          // 선택 모드 여부
    val selectedIds: Set<UUID> = emptySet(),    // 선택 된 아이템 ID
    val isMenuExpanded: Boolean = false,        // 더보기 메뉴 확장 여부
    val showDeleteDialog: Boolean = false,
    val showCreateFolderDialog: Boolean = false,
    val isBottomSheet: Boolean = false,
    val sheetMode: FileListSheetMode = FileListSheetMode.FOLDER_LIST,
    val createFolderName: String = "",
    val selectedFolder: FolderItem? = null,
    val folderId: UUID = EMPTY_UUID,
    val folderName: String = "",
    val errorMessage: String? = null,

    // 폴더 리스트
    val folderList: List<FolderItem> = emptyList()

)

enum class FileListSheetMode {
    FOLDER_LIST,  // 폴더 목록 모드
    CREATE_FOLDER // 새 폴더 입력 모드
}