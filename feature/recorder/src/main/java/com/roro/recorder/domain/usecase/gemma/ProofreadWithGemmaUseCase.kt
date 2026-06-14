package com.roro.recorder.domain.usecase.gemma

import com.roro.core.gemma.GemmaManager
import timber.log.Timber
import javax.inject.Inject

class ProofreadWithGemmaUseCase @Inject constructor(
    private val gemmaManager: GemmaManager
) {
    suspend operator fun invoke(text: String): String {
        return try {
            val prompt = """
                Correct only the spelling and spacing of the following STT text. Do not change the content. Output only the result.
                $text
            """.trimIndent()

            gemmaManager.generate(prompt)
        } catch (e: Exception) {
            Timber.tag("ProofreadGemma").e(e, "❌ 교정 실패 → 원본 반환")
            text
        }
    }
}