package com.roro.storage.domain

import com.roro.core.model.VoiceNote
import java.util.UUID
import javax.inject.Inject

class RemoveVoiceNoteUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend operator fun invoke(voiceNoteId: UUID) {
        repository.removeVoiceNote(voiceNoteId = voiceNoteId)
    }
}