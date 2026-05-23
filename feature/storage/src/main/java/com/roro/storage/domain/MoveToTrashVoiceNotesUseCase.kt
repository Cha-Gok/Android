package com.roro.storage.domain

import java.util.UUID
import javax.inject.Inject

class MoveToTrashVoiceNotesUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend operator fun invoke(voiceNoteIds: List<UUID>) {
        repository.moveToVoiceNotes(voiceNoteIds)
    }
}