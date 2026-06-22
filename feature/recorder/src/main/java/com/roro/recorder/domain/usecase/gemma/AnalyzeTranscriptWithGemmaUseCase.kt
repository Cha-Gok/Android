package com.roro.recorder.domain.usecase.gemma

import com.roro.core.gemma.GemmaManager
import javax.inject.Inject

// 요약 + 키워드 같이 생성
class AnalyzeTranscriptWithGemmaUseCase @Inject constructor(
    private val gemmaManager: GemmaManager
) {
    suspend operator fun invoke(text: String): AnalyzeTranscriptResult {
        val prompt = """
            Analyze the transcript below.
            Respond in the same language as the transcript.
            Use only information from the transcript.
            Do not add facts, assumptions, or explanations.

            Return exactly this format:

            SUMMARY:
            * first key point
            * second key point
            * third key point

            KEYWORDS:
            keyword1, keyword2, keyword3, keyword4, keyword5

            Rules:
            - Use exactly 3 summary bullet points.
            - Each summary bullet must start with "* ".
            - Use exactly 5 keywords.
            - Separate keywords with commas only.
            - Do not number keywords.
            - Do not include markdown code fences.
            - Do not add headings other than SUMMARY: and KEYWORDS:.

            TRANSCRIPT_START
            $text
            TRANSCRIPT_END
        """.trimIndent()

        return AnalyzeTranscriptParser.parse(gemmaManager.generate(prompt))
    }
}
