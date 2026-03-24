package com.roro.recorder.domain.repository

import com.roro.core.model.Transcript
import com.roro.core.model.VoiceNote
import com.roro.core.model.VoiceRecord
import kotlinx.coroutines.flow.Flow
import java.util.UUID

// "무엇을 할 수 있는지"만 정의. 실제 동작은 없다.
// VoiceNoteRepository → DB 저장/조회만


interface VoiceNoteRepository {
    // VoiceNote
    suspend fun createVoiceNote(voiceNote: VoiceNote): UUID
    suspend fun getVoiceNote(id: UUID): VoiceNote?
    suspend fun updateVoiceNote(voiceNote: VoiceNote)
    suspend fun deleteVoiceNote(id: UUID)

    // VoiceRecord
    suspend fun saveVoiceRecord(voiceRecord: VoiceRecord): UUID

    // Transcript
    suspend fun saveTranscript(transcript: Transcript)
    suspend fun getTranscript(voiceNoteId: UUID): Transcript?
}