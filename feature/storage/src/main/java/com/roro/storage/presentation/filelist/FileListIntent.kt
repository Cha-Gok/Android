package com.roro.storage.presentation.filelist

import com.roro.core.domain.model.FolderItem
import com.roro.core.domain.model.SortType
import com.roro.core.model.Folder
import java.util.UUID

sealed interface FileListIntent {
    // 1. 초기화 및 데이터 로드
    data class Initialize(val folderId: UUID, val folderName: String) : FileListIntent
    data object FetchFolderList : FileListIntent
    data class FetchVoiceNoteList(val folderId: UUID) : FileListIntent

    // 2. 기본 탑바 & 네비게이션
    data object ClickBack : FileListIntent // 뒤로가기 통합 제어
    data object ClickSearch : FileListIntent

    // 3. 더보기 메뉴 (정렬, 전체선택, 선택모드 진입)
    data class ShowMoreMenu(val isShow: Boolean) : FileListIntent
    data class ChangeSort(val sortType: SortType) : FileListIntent
    data object ToggleSelectAll : FileListIntent
    data object EnterSelectionMode : FileListIntent

    // 4. 선택(편집) 모드 상태 액션
    data class ToggleSelectItem(val id: UUID) : FileListIntent
    data object ExitSelectionMode : FileListIntent

    // 5. 파일 이동 액션 (바텀 시트)
    data class ShowBottomSheet(val isShow: Boolean) : FileListIntent
    data class SelectTargetFolder(val folder: FolderItem) : FileListIntent
    data object ConfirmMove : FileListIntent

    // 6. 삭제 다이얼로그 (삭제 확인용)
    data class ShowDeleteDialog(val isShow: Boolean) : FileListIntent
    data object ConfirmDelete : FileListIntent

    // 7. 새 폴더 생성 다이얼로그 (이동 바텀시트 내부 기능)
    data class ShowCreateFolderDialog(val isShow: Boolean) : FileListIntent

    data class ChangeSheetMode(val mode: FileListSheetMode) : FileListIntent
    data class UpdateNewFolderName(val name: String) : FileListIntent
    data object ConfirmCreateFolder : FileListIntent // 생성 후 다시 FOLDER_LIST 모드로 전환
}

sealed interface FileListEffect {
    data class ShowToast(val message: String) : FileListEffect
    data object NavigateBack : FileListEffect
    data object NavigateToSearch : FileListEffect // object 앞에 data 추가 권장
}