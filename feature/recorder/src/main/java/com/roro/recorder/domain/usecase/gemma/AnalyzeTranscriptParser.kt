package com.roro.recorder.domain.usecase.gemma

object AnalyzeTranscriptParser {
    fun parse(raw: String): AnalyzeTranscriptResult {
        val summaryStart = raw.indexOf("SUMMARY:", ignoreCase = true)
        val keywordsStart = raw.indexOf("KEYWORDS:", ignoreCase = true)

        val summaryText = if (summaryStart >= 0 && keywordsStart > summaryStart) {
            raw.substring(summaryStart + "SUMMARY:".length, keywordsStart)
        } else {
            raw
        }.trim()

        val keywordText = if (keywordsStart >= 0) {
            raw.substring(keywordsStart + "KEYWORDS:".length)
        } else {
            ""
        }

        return AnalyzeTranscriptResult(
            summaryText = summaryText,
            keywords = keywordText
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .take(5)
        )
    }
}
