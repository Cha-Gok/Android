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
                아래 텍스트는 음성 인식(STT)으로 생성된 결과입니다.
                맞춤법, 띄어쓰기, 어색한 표현을 자연스럽게 교정해주세요.
                내용은 절대 바꾸지 말고, 교정된 텍스트만 반환하세요.
                
                텍스트:
                $text
            """.trimIndent()

            gemmaManager.generate(prompt)
        } catch (e: Exception) {
            Timber.tag("ProofreadGemma").e(e, "❌ 교정 실패 → 원본 반환")
            text
        }
    }
}