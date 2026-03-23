package com.roro.recorder.domain.usecase

import com.roro.recorder.domain.repository.VoiceNoteRepository

// STT 결과 저장
class SaveTranscriptUseCase(
    private val repository: VoiceNoteRepository
) {
    suspend operator fun invoke(noteId: String, text: String) {
        //repository.updateTranscript(noteId, text)
    }
}