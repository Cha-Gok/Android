package com.roro.recorder.data.repository

import com.roro.core.dao.KeywordDao
import com.roro.core.dao.SummaryDao
import com.roro.core.dao.TranscriptDao
import com.roro.core.dao.VoiceNoteDao
import com.roro.core.dao.VoiceRecordDao
import com.roro.core.entity.KeywordEntity
import com.roro.core.entity.SummaryEntity
import com.roro.core.entity.TranscriptEntity
import com.roro.core.entity.VoiceNoteEntity
import com.roro.core.entity.VoiceRecordEntity
import com.roro.core.mapper.toEntity
import com.roro.core.model.Summary
import com.roro.core.model.Transcript
import com.roro.core.model.VoiceNote
import com.roro.core.model.VoiceRecord
import com.roro.recorder.data.datasource.RecordDataSource
import com.roro.recorder.domain.repository.RecordRepository
import java.io.File
import java.util.UUID
import javax.inject.Inject

/**
 * 기능 설명:
 * - 녹음 데이터 저장을 담당하는 Repository 구현체
 * - STT 완료 후 VoiceNote, VoiceRecord, Transcript, Summary, Keyword를 한번에 저장
 *
 * @author hyeonseo
 * @since 2026. 04. 12.
 */
class RecordRepositoryImpl @Inject constructor(
    private val voiceNoteDao: VoiceNoteDao,
    private val voiceRecordDao: VoiceRecordDao,
    private val transcriptDao: TranscriptDao,
    private val summaryDao: SummaryDao,
    private val keywordDao: KeywordDao,
) : RecordRepository {

    override suspend fun saveRecording(
        audioFile: File, // 녹음된 WAV 파일
        durationSec: Double, // 녹음 길이 (초)
        sttText: String, // 변환 결과 텍스트
        summaryText: String, // 요약 결과 텍스트
        keywords: List<String>, // 추출된 키워드 목록
        folderId: UUID? // 저장할 폴더 ID (null이면 기본 폴더)
    ): UUID {  // Unit → UUID 추가
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

        // 4. 요약문 저장
        if (summaryText.isNotBlank()) {
            summaryDao.insert(
                SummaryEntity(
                    id = UUID.randomUUID(),
                    voiceNoteId = voiceNoteId,
                    text = summaryText,
                    createdAt = now
                )
            )
        }

        // 5. 키워드 추출 및 저장 (sttText 기반 추출 결과)
        if (keywords.isNotEmpty()) {
            keywordDao.insertAll(
                keywords.map { word ->
                    KeywordEntity(
                        id = UUID.randomUUID(),
                        voiceNoteId = voiceNoteId,
                        word = word
                    )
                }
            )
        }

        return voiceNoteId  // ← 추가
    }

}