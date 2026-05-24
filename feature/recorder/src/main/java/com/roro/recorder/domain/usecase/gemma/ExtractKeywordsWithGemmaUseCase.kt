package com.roro.recorder.domain.usecase.gemma

import com.roro.recorder.data.GemmaManager
import timber.log.Timber
import javax.inject.Inject

class ExtractKeywordsWithGemmaUseCase @Inject constructor(
    private val gemmaManager: GemmaManager
) {
    suspend operator fun invoke(text: String): List<String> {
        return try {
            val prompt = """
                아래 텍스트에서 핵심 키워드 10개를 추출해주세요.
                쉼표로 구분해서 키워드만 출력하세요. 다른 말은 하지 마세요.
                예시: 키워드1, 키워드2, 키워드3, 키워드4, 키워드5
                
                텍스트:
                $text
            """.trimIndent()

            val result = gemmaManager.generate(prompt)
            result.split(",").map { it.trim() }.filter { it.isNotBlank() }.take(5)
        } catch (e: Exception) {
            Timber.e(e, "❌ Gemma 키워드 추출 실패")
            emptyList()
        }
    }
}