package com.roro.storage.presentation.search

import com.roro.core.domain.model.FolderItem
import com.roro.core.domain.model.VoiceNoteItem
import com.roro.core.navigation.SearchType

data class SearchUiState(
    val isLoading: Boolean = false,
    // 각 화면별로 보여줄 클래스가 필요하다..
    val itemList: List<SearchItem> = emptyList(), // 휴지통 리스트
    val errorMessage: String? = null,
    val searchType: SearchType = SearchType.HOME,
    val searchQuery: String = ""
)

sealed interface SearchItem {
    val id: String
    val title: String
    val createAt: String

    data class HomeSearchItem(
        val folderItem: FolderItem? = null,
        val voiceNoteItem: VoiceNoteItem? = null
    ) : SearchItem {
        override val id: String get() = folderItem?.id ?: voiceNoteItem?.id ?: ""
        override val title: String get() = folderItem?.title ?: voiceNoteItem?.title ?: ""
        override val createAt: String get() = folderItem?.createAt ?: voiceNoteItem?.createAt ?: ""


        // UI 표현을 위한 추가 헬퍼 프로퍼티
        val isVoiceNote: Boolean get() = voiceNoteItem != null
        val count: String? get() = folderItem?.count
        val duration: String? get() = voiceNoteItem?.duration
        val isFolder: Boolean get() = folderItem != null
        val folderName: String? get() = voiceNoteItem?.folderName
    }


    data class FolderSearchItem(
        val folderItem: FolderItem? = null,
    ) : SearchItem {
        override val id: String get() = folderItem?.id ?: ""
        override val title: String get() = folderItem?.title ?: ""
        override val createAt: String get() = folderItem?.createAt ?: ""

        // UI 표현을 위한 추가 헬퍼 프로퍼티
        val count: String? get() = folderItem?.count
    }

    data class TrashSearchItem(
        val folderItem: FolderItem? = null,
        val voiceNoteItem: VoiceNoteItem? = null
    ) : SearchItem {
        override val id: String get() = folderItem?.id ?: voiceNoteItem?.id ?: ""
        override val title: String get() = folderItem?.title ?: voiceNoteItem?.title ?: ""
        override val createAt: String get() = folderItem?.createAt ?: voiceNoteItem?.createAt ?: ""


        // UI 표현을 위한 추가 헬퍼 프로퍼티
        val isVoiceNote: Boolean get() = voiceNoteItem != null
        val count: String? get() = folderItem?.count
        val duration: String? get() = voiceNoteItem?.duration
        val isFolder: Boolean get() = folderItem != null
        val folderName: String? get() = voiceNoteItem?.folderName
    }
}