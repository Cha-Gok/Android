package com.roro.recorder.domain.usecase.gemma

import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyzeTranscriptParserTest {
    @Test
    fun parses_summary_and_keywords_sections() {
        val raw = """
            SUMMARY:
            * 첫 번째 요약
            * 두 번째 요약
            * 세 번째 요약

            KEYWORDS:
            회의, 일정, 결정, 담당자, 후속작업
        """.trimIndent()

        val result = AnalyzeTranscriptParser.parse(raw)

        assertEquals("* 첫 번째 요약\n* 두 번째 요약\n* 세 번째 요약", result.summaryText)
        assertEquals(listOf("회의", "일정", "결정", "담당자", "후속작업"), result.keywords)
    }

    @Test
    fun limits_keywords_to_five() {
        val raw = """
            SUMMARY:
            * 요약

            KEYWORDS:
            a, b, c, d, e, f
        """.trimIndent()

        val result = AnalyzeTranscriptParser.parse(raw)

        assertEquals(listOf("a", "b", "c", "d", "e"), result.keywords)
    }
}
