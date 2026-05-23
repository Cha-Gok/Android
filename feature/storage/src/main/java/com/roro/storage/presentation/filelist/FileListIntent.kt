package com.roro.storage.presentation.filelist

import com.roro.core.domain.model.SortType
import java.util.UUID

sealed interface FileListIntent {
    // 초기화면
    data class Initialize(val folderId: UUID, val folderName: String) : FileListIntent

    // 기본 탑바 & 메뉴 액션
    data object ClickSearch : FileListIntent
    data object ClickMore : FileListIntent
    data object DismissMoreMenu : FileListIntent

    // 드롭다운 메뉴 내 항목 클릭
    data class ChangeSort(val sortType: SortType) : FileListIntent
    data object ToggleSelectAll : FileListIntent
    data object ClickSelectMode : FileListIntent

    // 폴더 목록 가져오기
    data object FetchFolderList : FileListIntent

    // 선택 모드 상태에서 액션 (탑바 및 아이템 클릭)
    data object ClickCloseSelectMode : FileListIntent // 상단 X 버튼 클릭 (해제)
    data object ClickMoveItems : FileListIntent // 바텀 시트 올라옴
    data object DismissMoveSheet : FileListIntent // 바텀 시트 닫음
    data object ClickRemove : FileListIntent
    data class ClickSelectItem(val id: UUID) : FileListIntent

    // 다이얼로그 액션
    data object DialogCancel : FileListIntent
    data object DialogConfirm : FileListIntent

    data class FetchVoiceNoteList(val folderId: UUID) : FileListIntent

    data class MoveToTrashVoiceNotes(val voiceNoteIds: List<UUID>) : FileListIntent
}

sealed interface FileListEffect {
    data class ShowToast(val message: String) : FileListEffect
    object NavigateToSearch : FileListEffect
}