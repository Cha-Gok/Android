package com.roro.recorder.domain.usecase.gemma

import com.roro.core.gemma.GemmaManager
import javax.inject.Inject

class AnalyzeTranscriptWithGemmaUseCase @Inject constructor(
    private val gemmaManager: GemmaManager
) {
    suspend operator fun invoke(text: String): AnalyzeTranscriptResult {
        val prompt = """
            Analyze the transcript below.
            Respond in the same language as the transcript.
            Return exactly this format:

            SUMMARY:
            * first key point
            * second key point
            * third key point

            KEYWORDS:
            keyword1, keyword2, keyword3, keyword4, keyword5

            Transcript:
            $text
        """.trimIndent()

        return AnalyzeTranscriptParser.parse(gemmaManager.generate(prompt))
    }
}
