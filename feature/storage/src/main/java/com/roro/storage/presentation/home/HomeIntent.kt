package com.roro.storage.presentation.home

import com.roro.core.datastore.Language
import com.roro.core.model.Folder
import com.roro.core.model.VoiceNote
import java.util.UUID

/**
 * Home 화면 인텐트 (사용자 동작)
 *
 * 최근기록
 * 기본 폴더 (아이템 + 개수)
 * 개인 폴더 (개수)
 * 휴지통 (개수)
 *
 * @author sehoon
 * @since 2026. 5. 5.
 * @modified
 */
sealed interface HomeIntent {
    data object Initialize : HomeIntent

    // 녹음 화면 이동
    data object ClickRecordButton : HomeIntent

    // 4개 박스 클릭
    data class ClickFolderType(val type: DefaultFolderType) : HomeIntent

    // 파일목록 가져오기
    data class FetchVoiceNote(val folderId: UUID) : HomeIntent

    // 검색 클릭
    data object ClickSearch : HomeIntent

    // 설정 클릭
    data object ClickSetting : HomeIntent
    data object ClickTos : HomeIntent
    data class SelectLanguageOption(val language: Language) : HomeIntent
    data object ConfirmDialog : HomeIntent
    data object DismissDialog : HomeIntent
}

sealed interface HomeEffect {
    data class ShowToast(val message: String) : HomeEffect
    object NavigateToRecord : HomeEffect
    object NavigateToPrivate : HomeEffect
    object NavigateToTrash : HomeEffect
    object NavigateToSearch : HomeEffect
    object NavigateToTos : HomeEffect
}