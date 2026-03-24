package com.roro.recorder.data.repository

import com.roro.core.mapper.toEntity
import com.roro.core.model.Transcript
import com.roro.core.model.VoiceNote
import com.roro.core.model.VoiceRecord
import com.roro.recorder.data.datasource.RecordRoomDataSource
import com.roro.recorder.domain.repository.VoiceNoteRepository
import java.util.UUID
import javax.inject.Inject

// Room DAO를 domain으로 변환해주는 역할
// "어떻게 할 것인지"를 실제로 구현
class VoiceNoteRepositoryImpl @Inject constructor(
    private val room: RecordRoomDataSource
) : VoiceNoteRepository {

    // VoiceNote + VoiceRecord + Transcript 한번에 생성
    override suspend fun createVoiceAll(title: String, audioFilePath: String, durationSec: Double): VoiceNote {
        val now = System.currentTimeMillis()
        val voiceNoteId = UUID.randomUUID()

        val voiceNote = VoiceNote(
            id = voiceNoteId,
            title = title,
            createdAt = now,
            updatedAt = now,

        )

        val voiceRecord = VoiceRecord(
            id = UUID.randomUUID(),
            voiceNoteId = voiceNoteId,
            audioFilePath = audioFilePath,
            duration = durationSec,   // 🔥 핵심
            createdAt = now
        )

        val transcript = Transcript(
            id = UUID.randomUUID(),
            voiceNoteId = voiceNoteId,
            text = ""
        )

        room.createVoiceAll(
            voiceNote = voiceNote.toEntity(),
            voiceRecord = voiceRecord.toEntity(),
            transcript = transcript.toEntity()
        )

        return voiceNote
    }

    override suspend fun getVoiceNote(id: UUID): VoiceNote? =
        room.getVoiceNote(id)

    override suspend fun updateVoiceNote(note: VoiceNote) =
        room.updateVoiceNote(note.toEntity().copy(updatedAt = System.currentTimeMillis()))

    override suspend fun deleteVoiceNote(id: UUID) =
        room.deleteVoiceNote(id)

    override suspend fun saveVoiceRecord(record: VoiceRecord) =
        room.insertVoiceRecord(record.toEntity())

    override suspend fun getVoiceRecord(voiceNoteId: UUID): VoiceRecord? =
        room.getVoiceRecord(voiceNoteId)

    override suspend fun saveTranscript(transcript: Transcript) =
        room.updateTranscript(transcript.toEntity())

    override suspend fun getTranscript(voiceNoteId: UUID): Transcript? =
        room.getTranscript(voiceNoteId)
}