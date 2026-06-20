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
                Correct only spelling and spacing in the STT text below.
                Do not change meaning or add content.
                Preserve line breaks exactly.
                Each input line is one 30-second segment.
                Do not merge, split, remove, or reorder lines.
                Return only the corrected text.

                Text:
                $text
            """.trimIndent()

            val result = gemmaManager.generate(prompt)
            ProofreadLineValidator.choose(text, result)
        } catch (e: Exception) {
            Timber.tag("ProofreadGemma").e(e, "❌ 교정 실패 → 원본 반환")
            text
        }
    }
}
