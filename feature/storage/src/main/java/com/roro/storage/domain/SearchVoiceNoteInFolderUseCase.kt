package com.roro.storage.domain

import com.roro.core.domain.model.VoiceNoteItem
import com.roro.storage.presentation.search.SearchItem
import javax.inject.Inject

class SearchVoiceNoteInFolderUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend operator fun invoke(query: String, folderId: String): List<SearchItem.FileListSearchItem> {
        if (query.isBlank()) return emptyList()

        return repository.searchVoiceNotesInFolder(query = query, folderId = folderId).map {
            SearchItem.FileListSearchItem(voiceNoteItem = it)
        }
    }
}