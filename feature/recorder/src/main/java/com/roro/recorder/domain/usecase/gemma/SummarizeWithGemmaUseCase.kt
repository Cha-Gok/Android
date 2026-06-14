package com.roro.recorder.domain.usecase.gemma

import com.roro.core.gemma.GemmaManager
import timber.log.Timber
import javax.inject.Inject

class SummarizeWithGemmaUseCase @Inject constructor(
    private val gemmaManager: GemmaManager
) {
    suspend operator fun invoke(text: String): String {
        return try {
            val prompt = """
                Summarize the text below into exactly 3 key points.
                Each key point must start with "*" and be a single sentence.
                Respond in the same language as the input text.
                Return ONLY the 3 key points. Do not add any explanation or extra text.
                
                Text:
                $text
            """.trimIndent()

            gemmaManager.generate(prompt)
        } catch (e: Exception) {
            Timber.e(e, "❌ Gemma 요약 실패")
            text
        }
    }
}