package com.roro.recorder.domain.usecase.gemma

import com.roro.recorder.data.GemmaManager
import timber.log.Timber
import javax.inject.Inject

class SummarizeWithGemmaUseCase @Inject constructor(
    private val gemmaManager: GemmaManager
) {
    suspend operator fun invoke(text: String): String {
        return try {
            val prompt = """
                아래 텍스트를 핵심 내용 3줄로 요약해주세요.
                각 줄은 "*"로 시작하고 한 문장으로 작성하세요.
                요약문만 출력하고 다른 말은 하지 마세요.
                
                텍스트:
                $text
            """.trimIndent()

            gemmaManager.generate(prompt)
        } catch (e: Exception) {
            Timber.e(e, "❌ Gemma 요약 실패")
            text
        }
    }
}