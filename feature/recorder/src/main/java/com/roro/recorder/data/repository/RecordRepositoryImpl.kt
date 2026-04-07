package com.roro.recorder.data.repository

import com.roro.core.dao.TranscriptDao
import com.roro.core.dao.VoiceNoteDao
import com.roro.core.dao.VoiceRecordDao
import com.roro.core.entity.TranscriptEntity
import com.roro.core.entity.VoiceNoteEntity
import com.roro.core.entity.VoiceRecordEntity
import com.roro.core.mapper.toEntity
import com.roro.core.model.Summary
import com.roro.core.model.Transcript
import com.roro.core.model.VoiceNote
import com.roro.core.model.VoiceRecord
import com.roro.recorder.data.datasource.LocalDataSource
import com.roro.recorder.data.datasource.RecordDataSource
import com.roro.recorder.domain.repository.RecordRepository
import com.roro.storage.data.datasource.RecorderLocalFileDataSource
import java.io.File
import java.util.UUID
import javax.inject.Inject

class RecordRepositoryImpl @Inject constructor(
    private val voiceNoteDao: VoiceNoteDao,
    private val voiceRecordDao: VoiceRecordDao,
    private val transcriptDao: TranscriptDao,
) : RecordRepository {

    override suspend fun saveRecording(
        audioFile: File,
        durationSec: Double,
        sttText: String,
        folderId: UUID?
    ) {
        val now = System.currentTimeMillis()
        val voiceNoteId = UUID.randomUUID()

        // 1. VoiceNote 생성
        voiceNoteDao.insert(
            VoiceNoteEntity(
                id = voiceNoteId,
                title = audioFile.nameWithoutExtension,
                createdAt = now,
                updatedAt = now,
                deletedAt = null,
                folderId = folderId
            )
        )

        // 2. VoiceRecord 저장
        voiceRecordDao.insert(
            VoiceRecordEntity(
                id = UUID.randomUUID(),
                voiceNoteId = voiceNoteId,
                audioPath = audioFile.absolutePath,
                durationSec = durationSec,
                createdAt = now
            )
        )

        // 3. Transcript 저장
        if (sttText.isNotBlank()) {
            transcriptDao.insert(
                TranscriptEntity(
                    id = UUID.randomUUID(),
                    voiceNoteId = voiceNoteId,
                    text = sttText,
                    createdAt = now
                )
            )
        }
    }
}