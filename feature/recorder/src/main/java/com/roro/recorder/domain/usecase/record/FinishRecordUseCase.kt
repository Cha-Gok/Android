package com.roro.recorder.domain.usecase.record

import com.roro.core.model.Transcript
import com.roro.core.model.VoiceNote
import com.roro.core.model.VoiceRecord
import com.roro.recorder.domain.repository.RecordRepository
import com.roro.recorder.domain.repository.VoiceNoteRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

// 저장 및 stt 변환


//FinishRecordUseCase 호출
//↓
//1. VoiceNote 생성 (title, createdAt)
//↓
//2. VoiceRecord 생성 (audioFilePath, duration, voiceNoteId)
//↓
//3. RoomDB에 VoiceNote + VoiceRecord 저장

class FinishRecordUseCase @Inject constructor(
    private val recordRepository: RecordRepository,
    private val voiceNoteRepository: VoiceNoteRepository
) {
    suspend operator fun invoke() {
        recordRepository.finishRecording()

        val voiceNoteId = UUID.randomUUID()
        val now = System.currentTimeMillis()
        val title = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
            .format(Date(now))

        voiceNoteRepository.createVoiceNote(
            VoiceNote(id = voiceNoteId, title = title, createdAt = now)
        )
        voiceNoteRepository.saveVoiceRecord(
            VoiceRecord(
                audioFilePath = recordRepository.getAudioFilePath(),
                duration = recordRepository.getDuration(),
                voiceNoteId = voiceNoteId
            )
        )
        voiceNoteRepository.saveTranscript(
            Transcript(
                text = recordRepository.observeTranscript().first(),
                voiceNoteId = voiceNoteId
            )
        )
    }
}