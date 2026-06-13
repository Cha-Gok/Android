package com.roro.storage.presentation.home

import com.roro.core.datastore.Language
import com.roro.core.domain.model.VoiceNoteItem
import com.roro.core.model.Folder
import com.roro.core.model.VoiceNote

/**
 * 홈화면 uiState 정의
 *
 * @author sehoon
 * @since 2026. 5. 5.
 * @modified
 */
data class HomeUiState(
    val selectedFolderType: DefaultFolderType = DefaultFolderType.RECENT,
    val isLoading: Boolean = false,
    val isDialog: Boolean = false,
    val selectedTempLanguage: Language = Language.KOREAN,

    // [통합 관리] 현재 화면 하단 리스트 영역에 보여줄 데이터 (최근 기록5개 or 기본 폴더 n개)
    val displayVoiceNotes: List<VoiceNoteItem> = emptyList(),
//    val item: List<VoiceNoteItem> = emptyList(),

    val defaultFolderCount: Int = 0, // 기본 폴더 내 아이템 개수
    val privateFolderCount: Int = 0, // 개인 폴더 (사용자 폴더)
    val trashCount: Int = 0,// 휴지통 개수 (삭제된 폴더의 수 + 삭제된 파일의 수)
    val defaultFolderItem: List<Folder> = emptyList(), // 기본 폴더 아이템
    val errorMessage: String? = null
)
