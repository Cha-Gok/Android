package com.roro.storage.presentation.folderlist

import com.roro.core.domain.model.FolderItem
import java.util.UUID

sealed interface PrivateFolderIntent {
    // 1. 초기화 및 데이터 로드
    data object Initialize : PrivateFolderIntent
    data object FetchFolderList : PrivateFolderIntent

    // 2. 기본 탑바 & 네비게이션
    data object ClickBack : PrivateFolderIntent
    data class ClickPrivateFolder(val folderId: FolderItem) : PrivateFolderIntent
    data object ClickSearch : PrivateFolderIntent

    // 3. 수정 & 삭제
    data class RemoveFolder(val folder: FolderItem) : PrivateFolderIntent

    // 4. 새 폴더 다이얼로그
    data class ShowCreateFolderDialog(val isShow: Boolean) : PrivateFolderIntent
    data class InputFolderName(val input:String): PrivateFolderIntent

    // 5. 수정 다이얼로그
    data class ShowModifyFolderDialog(val folder: FolderItem) : PrivateFolderIntent

    // 6. 다이얼로그 닫기
    data object DismissDialog: PrivateFolderIntent

    // 7. 다이얼로그 확인
    data object ConfirmDialog: PrivateFolderIntent

    // 스와이프 상태 관리
    data class OnFolderSwipe(val folder: FolderItem?): PrivateFolderIntent

}

sealed interface PrivateFolderEffect {
    data class ShowToast(val message: String) : PrivateFolderEffect
    data object NavigateBack : PrivateFolderEffect
    data object NavigateSearch : PrivateFolderEffect
    data class NavigateFileList(val folderId: String, val folderName: String) : PrivateFolderEffect
}