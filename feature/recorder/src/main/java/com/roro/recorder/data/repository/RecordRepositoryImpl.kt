package com.roro.recorder.data.repository

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
    private val recordDataSource: RecordDataSource,
    private val recorderLocalFileDataSource: RecorderLocalFileDataSource,
    private val localDataSource: LocalDataSource
) : RecordRepository {

    override suspend fun processAndSave(audioFile: File, folderId: UUID?): String {

        // ✅ 파일 검증 추가
        if (!audioFile.exists() || audioFile.length() == 0L) {
            throw IllegalStateException("녹음 파일이 비어있거나 존재하지 않음: ${audioFile.absolutePath}")
        }

        val now = System.currentTimeMillis()
        val voiceNoteId = UUID.randomUUID()

        return ""

        // 1. STT
        //val text = recordDataSource.getTranscript(audioFile)

        // 2. 요약
        //val summaryText = recordDataSource.summarize(text)

        // 3. Entity 생성
//        val voiceNote = VoiceNote(
//            id = voiceNoteId,
//            title = summaryText,
//            createdAt = now,
//            updatedAt = now,
//            folderId = folderId
//        )
//
//        val voiceRecord = VoiceRecord(
//            id = UUID.randomUUID(),
//            audioFilePath = audioFile.absolutePath,
//            duration = 0.0,
//            createdAt = now,
//            voiceNoteId = voiceNoteId
//        )
//
//        val transcript = Transcript(
//            id = UUID.randomUUID(),
//            text = text,
//            voiceNoteId = voiceNoteId
//        )
//
//        val summary = Summary(
//            id = UUID.randomUUID(),
//            text = summaryText,
//            voiceNoteId = voiceNoteId
//        )
//
//        // 4. 저장
//        localDataSource.saveAll(
//            note = voiceNote.toEntity(),
//            transcript = transcript.toEntity(),
//            summary = summary.toEntity()
//        )

        //return summaryText
    }

    // voiceNote 음성 파일 저장


}