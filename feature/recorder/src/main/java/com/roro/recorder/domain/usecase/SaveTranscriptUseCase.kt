package com.roro.recorder.domain.usecase

import com.roro.core.model.Transcript
import com.roro.recorder.domain.repository.VoiceNoteRepository
import java.util.UUID
import javax.inject.Inject

// STT 결과 저장
class SaveTranscriptUseCase @Inject constructor(
    private val repository: VoiceNoteRepository
) {
    suspend operator fun invoke(
        voiceNoteId: UUID,
        text: String
    ) {
        repository.saveTranscript(
            Transcript(
                voiceNoteId = voiceNoteId,
                text = text
            )
        )
    }
}