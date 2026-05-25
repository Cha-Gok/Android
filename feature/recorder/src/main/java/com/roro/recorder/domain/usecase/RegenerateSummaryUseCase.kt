package com.roro.recorder.domain.usecase

import com.roro.core.dao.KeywordDao
import com.roro.core.dao.SummaryDao
import com.roro.core.entity.KeywordEntity
import com.roro.recorder.domain.usecase.gemma.ExtractKeywordsWithGemmaUseCase
import com.roro.recorder.domain.usecase.gemma.SummarizeWithGemmaUseCase
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

/**
 * 기능 설명:
 * - 재생성 버튼 클릭 시 요약문 + 키워드를 다시 생성하고 DB에 업데이트
 * - SummarizeTextSimpleUseCase: 요약 재생성
 * - ExtractKeywordsUseCase: 키워드 재추출
 *
 * @author
 * @since 2026. 04. 19.
 */

// 0512 젬마 버전으로 수정
class RegenerateSummaryUseCase @Inject constructor(
//    private val summarizeTextSimpleUseCase: SummarizeTextSimpleUseCase,
//    private val extractKeywordsUseCase: ExtractKeywordsUseCase,
    private val summaryDao: SummaryDao,
    private val keywordDao: KeywordDao,

    private val summarizeWithGemmaUseCase: SummarizeWithGemmaUseCase,      // 교체
    private val extractKeywordsWithGemmaUseCase: ExtractKeywordsWithGemmaUseCase,  // 교체
) {
    data class Result(
        val summaryText: String,
        val keywords: List<String>
    )

    suspend operator fun invoke(
        voiceNoteId: UUID,
        sttText: String
    ): Result {
        // 1. 요약 재생성
        val newSummary = summarizeWithGemmaUseCase(sttText)
        Timber.tag("RegenerateSummary").d("요약 재생성 완료")

        // 2. 키워드 재추출
        val newKeywords = extractKeywordsWithGemmaUseCase(sttText)
        Timber.tag("RegenerateSummary").d("키워드 재추출 완료: $newKeywords")

        // 3. DB 업데이트 - 요약문 텍스트 갱신
        summaryDao.updateText(
            voiceNoteId = voiceNoteId,
            text = newSummary
        )

        // 4. DB 업데이트 - 기존 키워드 삭제 후 새로 삽입
        keywordDao.deleteByVoiceNoteId(voiceNoteId)
        keywordDao.insertAll(
            newKeywords.map { word ->
                KeywordEntity(
                    id = UUID.randomUUID(),
                    voiceNoteId = voiceNoteId,
                    word = word
                )
            }
        )

        return Result(
            summaryText = newSummary,
            keywords = newKeywords
        )
    }
}