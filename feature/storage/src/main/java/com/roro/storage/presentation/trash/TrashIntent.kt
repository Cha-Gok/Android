package com.roro.storage.presentation.trash

import java.util.UUID

sealed interface TrashIntent {
    // 1. 기본 탑바 & 메뉴 액션
    data object ClickSearch : TrashIntent
    data object ClickMore : TrashIntent
    data object DismissMoreMenu : TrashIntent

    // 2. 드롭다운 메뉴 내 항목 클릭
    data object ClickSelectMode : TrashIntent    // 드롭다운에서 '선택하기' 클릭 (진입)
    data object ClickSelectAllItem : TrashIntent // 드롭다운에서 '전체 선택' 클릭
    data object ClickEmptyTrash : TrashIntent    // 드롭다운에서 '휴지통 비우기' 클릭 (다이얼로그 띄우기)

    // 3. 선택 모드 상태에서의 액션 (탑바 및 아이템 클릭)
    data object ClickCloseSelectMode : TrashIntent // 상단 X 버튼 클릭 (해제)
    data object ClickRestoreItems : TrashIntent    // 상단 '복원' 클릭
    data object ClickRemoveItems : TrashIntent     // 상단 '삭제' 클릭 (영구 삭제)
    data class ClickSelectItem(val id: UUID) : TrashIntent // 체크박스 토글

    // 4. 다이얼로그 액션
    data object DialogCancel : TrashIntent  // 취소 버튼
    data object DialogConfirm : TrashIntent // 확인(비우기/삭제) 버튼

    // 5. 일반 모드 아이템 클릭
    data class ClickVoiceNote(val voiceNoteId: String) : TrashIntent
    data class ClickFolder(val folderId: String, val folderName: String) : TrashIntent
}

sealed interface TrashEffect {
    data class ShowToast(val message: String) : TrashEffect
    data object NavigateToSearch : TrashEffect
    data class NavigateToDetail(val voiceNoteId: String) : TrashEffect
    data class NavigateToFolder(val folderId: String, val folderName: String) : TrashEffect
}