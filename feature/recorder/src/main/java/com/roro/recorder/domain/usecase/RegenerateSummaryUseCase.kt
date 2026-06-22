package com.roro.recorder.domain.usecase

import com.roro.core.dao.KeywordDao
import com.roro.core.dao.SummaryDao
import com.roro.core.entity.KeywordEntity
import com.roro.recorder.domain.usecase.gemma.AnalyzeTranscriptWithGemmaUseCase
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class RegenerateSummaryUseCase @Inject constructor(
    private val summaryDao: SummaryDao,
    private val keywordDao: KeywordDao,
    private val analyzeTranscriptWithGemmaUseCase: AnalyzeTranscriptWithGemmaUseCase,
) {
    data class Result(
        val summaryText: String,
        val keywords: List<String>,
    )

    suspend operator fun invoke(
        voiceNoteId: UUID,
        sttText: String,
    ): Result {
        val analysis = analyzeTranscriptWithGemmaUseCase(sttText)
        Timber.tag("RegenerateSummary").d("analysis regenerated: $analysis")

        summaryDao.updateText(
            voiceNoteId = voiceNoteId,
            text = analysis.summaryText,
        )

        keywordDao.deleteByVoiceNoteId(voiceNoteId)
        keywordDao.insertAll(
            analysis.keywords.map { word ->
                KeywordEntity(
                    id = UUID.randomUUID(),
                    voiceNoteId = voiceNoteId,
                    word = word,
                )
            },
        )

        return Result(
            summaryText = analysis.summaryText,
            keywords = analysis.keywords,
        )
    }
}
