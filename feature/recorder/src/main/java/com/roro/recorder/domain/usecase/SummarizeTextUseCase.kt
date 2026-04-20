package com.roro.recorder.domain.usecase

import android.content.Context
import androidx.concurrent.futures.await
import com.google.mlkit.genai.summarization.Summarization
import com.google.mlkit.genai.summarization.SummarizationRequest
import com.google.mlkit.genai.summarization.SummarizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject


// 사용 X
/**
 * 기능 설명:
 * - 텍스트 요약을 담당하는 UseCase
 * - 긴 텍스트는 문장 단위로 청크 분할 후 각각 요약
 * - MLKit Summarization으로 영어 요약 후 한국어로 번역하여 반환
 *
 * @author hyeonseo
 * @since 2026. 04. 12.
 */
class SummarizeTextUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val translateTextUseCase: TranslateTextUseCase
) {
    companion object {
        private const val MAX_CHARS = 500
    }

    /**
     * 텍스트 요약 실행
     * - 문장 단위로 청크 분할 후 각각 요약
     * - 요약 결과를 합쳐 한국어로 번역하여 반환
     *
     * @param text 요약할 텍스트 (STT 결과)
     * @return 한국어로 번역된 요약 결과
     *
     * @author hyeonseo
     * @since 2026. 04. 12.
     * @modified
     */
    suspend operator fun invoke(text: String): String {
        // 1. 청크 분할
        val chunks = splitBySentence(text)

        // 2. 청크별 요약 후 합치기
        val finalSummary = chunks.map { chunk ->
            summarize(chunk)
        }.joinToString("\n")

        // 3. 영어 → 한국어 번역
        return translateTextUseCase(finalSummary)
    }

    /**
     * MLKit Summarization으로 텍스트 요약
     * - 요약 실패 시 원본 텍스트 반환
     *
     * @param text 요약할 청크 텍스트
     * @return 요약된 텍스트, 실패 시 원본 텍스트
     *
     * @author hyeonseo
     * @since 2026. 04. 12.
     * @modified
     */
    private suspend fun summarize(text: String): String {
        val options = SummarizerOptions.builder(context)
            .setInputType(SummarizerOptions.InputType.CONVERSATION)
            .setOutputType(SummarizerOptions.OutputType.THREE_BULLETS)  // 청크별 요약(3문장)
            .setLanguage(SummarizerOptions.Language.ENGLISH)
            .build()
        val summarizer = Summarization.getClient(options)
        return try {
            summarizer.prepareInferenceEngine().await()
            val request = SummarizationRequest.builder(text).build()
            summarizer.runInference(request).await().summary
        } catch (e: Exception) {
            Timber.tag("SummarizeUseCase").e(e, "❌ 요약 실패 → 원본 반환")
            text  // 요약 실패 시 원본 텍스트 반환
        } finally {
            summarizer.close()
        }
    }


    /**
     * 텍스트를 문장 단위로 청크 분할
     * - 마침표, 느낌표, 물음표 기준으로 분리
     * - MAX_CHARS 초과 시 새로운 청크로 분리
     *
     * @param text 분할할 텍스트
     * @return 청크 목록
     *
     * @author hyeonseo
     * @since 2026. 04. 12.
     * @modified
     */
    private fun splitBySentence(text: String): List<String> {
        val sentences = text.split(Regex("(?<=[.!?。])\\s+"))
        val chunks = mutableListOf<String>()
        var current = StringBuilder()

        for (sentence in sentences) {
            if (current.length + sentence.length > MAX_CHARS) {
                if (current.isNotEmpty()) chunks.add(current.toString())
                current = StringBuilder()
            }
            current.append(sentence).append(" ")
        }
        if (current.isNotEmpty()) chunks.add(current.toString())
        return chunks
    }
}