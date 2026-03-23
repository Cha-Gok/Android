package com.roro.recorder.domain.usecase

import com.roro.core.model.VoiceNote
import com.roro.core.model.VoiceRecord
import com.roro.recorder.domain.repository.VoiceNoteRepository
import com.roro.recorder.domain.repository.VoiceRecordRepository


// 녹음 완료 후 저장
class SaveVoiceRecordUseCase(
    private val voiceNoteRepository: VoiceNoteRepository,
    private val voiceRecordRepository: VoiceRecordRepository
) {
    suspend operator fun invoke(title: String, audioFilePath: String): VoiceNote {
        // 1. VoiceNote 생성 및 DB 저장
        val note = voiceNoteRepository.createVoiceNote(title)

        // 2. VoiceRecord 생성 및 DB 저장
        voiceRecordRepository.saveVoiceRecord(
            VoiceRecord(
                voiceNoteId = note.id,
                audioFilePath = audioFilePath
            )
        )

        return note
    }
}