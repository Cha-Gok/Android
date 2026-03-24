//package com.roro.recorder.data.datasource
//
//import com.roro.core.dao.TranscriptDao
//import com.roro.core.dao.VoiceNoteDao
//import com.roro.core.dao.VoiceRecordDao
//import com.roro.core.entity.TranscriptEntity
//import com.roro.core.entity.VoiceNoteEntity
//import com.roro.core.entity.VoiceRecordEntity
//import com.roro.core.mapper.toModel
//import com.roro.core.model.Transcript
//import com.roro.core.model.VoiceNote
//import com.roro.core.model.VoiceRecord
//import java.util.UUID
//import javax.inject.Inject
//
//class RecordRoomDataSource @Inject constructor(
//    private val voiceNoteDao: VoiceNoteDao,
//    private val voiceRecordDao: VoiceRecordDao,
//    private val transcriptDao: TranscriptDao
//) {
//
//
//    // VoiceNote + VoiceRecord + Transcript 한번에 생성
//    // 삭제 예정 -> 그냥 따로따로 선언 후 3개 생성을 진행
//    suspend fun createVoiceAll(
//        voiceNote: VoiceNoteEntity,
//        voiceRecord: VoiceRecordEntity,
//        transcript: TranscriptEntity
//    ) {
//        voiceNoteDao.insert(voiceNote)
//        voiceRecordDao.insert(voiceRecord)
//        transcriptDao.insert(transcript)
//    }
//
//    // VoiceNote
//    suspend fun insertVoiceNote(voiceNote: VoiceNoteEntity) =
//        voiceNoteDao.upsert(voiceNote)
//
//    suspend fun getVoiceNote(id: UUID): VoiceNote? =
//        voiceNoteDao.getNote(id)?.toModel()
//
//    suspend fun updateVoiceNote(voiceNote: VoiceNoteEntity) =
//        voiceNoteDao.upsert(voiceNote)
//
//    suspend fun deleteVoiceNote(id: UUID) {
//        val entity = voiceNoteDao.getNote(id) ?: return
//        voiceNoteDao.delete(entity)
//    }
//
//    // VoiceRecord
//    suspend fun insertVoiceRecord(voiceRecord: VoiceRecordEntity) =
//        voiceRecordDao.upsert(voiceRecord)
//
//    suspend fun getVoiceRecord(voiceNoteId: UUID): VoiceRecord? =
//        voiceRecordDao.getByVoiceNoteId(voiceNoteId)?.toModel()
//
//    // Transcript
//    suspend fun insertTranscript(transcript: TranscriptEntity) =
//        transcriptDao.insert(transcript)
//
//    suspend fun getTranscript(voiceNoteId: UUID): Transcript? =
//        transcriptDao.getByVoiceNoteId(voiceNoteId)?.toModel()
//
//    // Transcript 업데이트 (STT 결과 저장)
//    suspend fun updateTranscript(transcript: TranscriptEntity) =
//        transcriptDao.upsert(transcript)
//}