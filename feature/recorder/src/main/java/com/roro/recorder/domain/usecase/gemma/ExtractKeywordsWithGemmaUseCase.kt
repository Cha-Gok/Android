package com.roro.recorder.domain.usecase.gemma


import com.roro.core.gemma.GemmaManager
import timber.log.Timber
import javax.inject.Inject

class ExtractKeywordsWithGemmaUseCase @Inject constructor(
    private val gemmaManager: GemmaManager
) {
    suspend operator fun invoke(text: String): List<String> {
        return try {
            val prompt = """
                Extract 5 key keywords from the text below.
                Respond in the same language as the input text.
                Output only the keywords separated by commas. Do not add any explanation.
                Text: $text
            """.trimIndent()

            val result = gemmaManager.generate(prompt)
            result.split(",").map { it.trim() }.filter { it.isNotBlank() }.take(5)
        } catch (e: Exception) {
            Timber.e(e, "❌ Gemma 키워드 추출 실패")
            emptyList()
        }
    }
}