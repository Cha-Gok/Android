package com.roro.recorder.domain.usecase

import com.roro.core.model.VoiceNote
import com.roro.recorder.domain.repository.VoiceNoteRepository

//녹음 시작 + 노트 생성
class CreateVoiceNoteUseCase(
    private val repository: VoiceNoteRepository
) {
    suspend operator fun invoke(title: String): VoiceNote {
        return repository.createVoiceNote(title)
    }
}