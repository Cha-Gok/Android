package com.roro.recorder.domain.usecase

import com.roro.recorder.domain.repository.RecordRepository
import java.util.UUID
import javax.inject.Inject

class MoveToFolderUseCase @Inject constructor(
    private val repository: RecordRepository
) {
    suspend operator fun invoke(voiceNoteId: List<UUID>, folderId: String) {
        repository.moveToFolder(voiceNoteId = voiceNoteId, folderId = folderId)
    }
}