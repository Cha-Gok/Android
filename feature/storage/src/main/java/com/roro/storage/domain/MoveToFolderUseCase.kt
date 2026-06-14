package com.roro.storage.domain

import java.util.UUID
import javax.inject.Inject

class MoveToFolderUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend operator fun invoke(voiceNoteId: List<UUID>, folderId: String) {
        repository.moveToFolder(voiceNoteId = voiceNoteId, folderId = folderId)
    }
}