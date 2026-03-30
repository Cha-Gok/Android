package com.roro.recorder.data.datasource

import com.roro.core.dao.VoiceNoteDao
import com.roro.core.dao.TranscriptDao
import com.roro.core.dao.SummaryDao
import com.roro.core.entity.VoiceNoteEntity
import com.roro.core.entity.TranscriptEntity
import com.roro.core.entity.SummaryEntity
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val voiceNoteDao: VoiceNoteDao,
    private val transcriptDao: TranscriptDao,
    private val summaryDao: SummaryDao
) {

    suspend fun insertVoiceNote(note: VoiceNoteEntity) {
        voiceNoteDao.insert(note)
    }

    suspend fun insertTranscript(transcript: TranscriptEntity) {
        transcriptDao.insert(transcript)
    }

    suspend fun insertSummary(summary: SummaryEntity) {
        summaryDao.insert(summary)
    }

    /**
     * 한 번에 저장 (추천)
     */
    suspend fun saveAll(
        note: VoiceNoteEntity,
        transcript: TranscriptEntity,
        summary: SummaryEntity
    ) {
        voiceNoteDao.insert(note)
        transcriptDao.insert(transcript)
        summaryDao.insert(summary)
    }
}