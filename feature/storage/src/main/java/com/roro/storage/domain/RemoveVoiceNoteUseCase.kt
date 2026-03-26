package com.roro.storage.domain

import com.roro.core.model.VoiceNote
import javax.inject.Inject

class RemoveVoiceNoteUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend operator fun invoke(voiceNote: VoiceNote) {
        repository.removeVoiceNote(voiceNote = voiceNote)
    }
}