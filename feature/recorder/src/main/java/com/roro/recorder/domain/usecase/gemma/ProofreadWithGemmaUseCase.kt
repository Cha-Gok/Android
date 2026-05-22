package com.roro.recorder.domain.usecase.gemma

import com.roro.recorder.data.GemmaManager
import timber.log.Timber
import javax.inject.Inject

class ProofreadWithGemmaUseCase @Inject constructor(
    private val gemmaManager: GemmaManager
) {
    suspend operator fun invoke(text: String): String {
        return try {
            val prompt = """
            다음 STT 텍스트의 맞춤법과 띄어쓰기만 교정해. 내용 변경 금지. 결과만 출력.
            
            $text
        """.trimIndent()

            gemmaManager.generate(prompt)
        } catch (e: Exception) {
            Timber.tag("ProofreadGemma").e(e, "❌ 교정 실패 → 원본 반환")
            text
        }
    }
}