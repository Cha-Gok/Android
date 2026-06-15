package com.roro.recorder.domain.usecase

import com.roro.recorder.domain.repository.RecordRepository
import java.util.UUID
import javax.inject.Inject

class MoveToTrashVoiceNotesUseCase @Inject constructor(
    private val repository: RecordRepository
) {
    suspend operator fun invoke(voiceNoteIds: List<UUID>) {
        repository.moveToVoiceNotes(voiceNoteIds)
    }
}