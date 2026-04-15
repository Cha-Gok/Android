package com.roro.recorder.domain.usecase

import android.content.Context
import androidx.concurrent.futures.await
import com.google.mlkit.genai.summarization.Summarization
import com.google.mlkit.genai.summarization.SummarizationRequest
import com.google.mlkit.genai.summarization.SummarizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

/**
 * 기능 설명:
 * - 텍스트 전체를 한번에 3줄 요약하는 UseCase
 * - 청크 분할 없이 전체 텍스트를 바로 요약
 *
 * @author
 * @since 2026. 04. 14.
 */
class SummarizeTextSimpleUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val translateTextUseCase: TranslateTextUseCase
) {
    suspend operator fun invoke(text: String): String {
        val options = SummarizerOptions.builder(context)
            .setInputType(SummarizerOptions.InputType.CONVERSATION)
            .setOutputType(SummarizerOptions.OutputType.THREE_BULLETS) // 3줄 요약
            .setLanguage(SummarizerOptions.Language.ENGLISH)
            .build()

        val summarizer = Summarization.getClient(options)

        return try {
            summarizer.prepareInferenceEngine().await()
            val request = SummarizationRequest.builder(text).build()
            val summary = summarizer.runInference(request).await().summary
            translateTextUseCase(summary) // 영어 → 한국어
        } catch (e: Exception) {
            Timber.tag("SummarizeSimple").e(e, "❌ 요약 실패 → 원본 반환")
            text
        } finally {
            summarizer.close()
        }
    }
}