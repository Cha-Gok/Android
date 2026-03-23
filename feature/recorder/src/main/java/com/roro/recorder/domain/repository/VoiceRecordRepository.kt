package com.roro.recorder.domain.repository

import com.roro.core.model.VoiceRecord
import java.io.File
import java.util.UUID

interface VoiceRecordRepository {
    fun startRecording()
    fun stopRecording(): File  // 녹음 완료된 파일 반환
    fun isRecording(): Boolean

    suspend fun saveVoiceRecord(record: VoiceRecord)
    suspend fun getVoiceRecord(voiceNoteId: UUID): VoiceRecord?
}