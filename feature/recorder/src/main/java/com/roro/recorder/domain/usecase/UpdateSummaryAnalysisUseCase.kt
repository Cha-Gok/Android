package com.roro.recorder.domain.usecase

import com.roro.core.dao.KeywordDao
import com.roro.core.dao.SummaryDao
import com.roro.core.dao.VoiceNoteDao
import com.roro.core.domain.model.SummaryStatus
import com.roro.core.entity.KeywordEntity
import com.roro.core.entity.SummaryEntity
import com.roro.recorder.domain.usecase.gemma.AnalyzeTranscriptResult
import java.util.UUID
import javax.inject.Inject

class UpdateSummaryAnalysisUseCase @Inject constructor(
    private val voiceNoteDao: VoiceNoteDao,
    private val summaryDao: SummaryDao,
    private val keywordDao: KeywordDao
) {
    suspend operator fun invoke(
        voiceNoteId: UUID,
        analysis: AnalyzeTranscriptResult
    ) {
        if (analysis.summaryText.isBlank()) {
            markFailed(voiceNoteId)
            return
        }

        val now = System.currentTimeMillis()
        summaryDao.upsert(
            SummaryEntity(
                id = UUID.randomUUID(),
                voiceNoteId = voiceNoteId,
                text = analysis.summaryText,
                createdAt = now
            )
        )
        keywordDao.deleteByVoiceNoteId(voiceNoteId)
        if (analysis.keywords.isNotEmpty()) {
            keywordDao.insertAll(
                analysis.keywords.map { word ->
                    KeywordEntity(
                        id = UUID.randomUUID(),
                        voiceNoteId = voiceNoteId,
                        word = word
                    )
                }
            )
        }
        voiceNoteDao.updateSummaryStatus(voiceNoteId, SummaryStatus.SUCCESS, now)
    }

    suspend fun markFailed(voiceNoteId: UUID) {
        voiceNoteDao.updateSummaryStatus(
            voiceNoteId = voiceNoteId,
            status = SummaryStatus.FAIL,
            updatedAt = System.currentTimeMillis()
        )
    }
}
