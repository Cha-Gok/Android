package com.roro.recorder.domain.usecase.gemma

object SummaryEligibility {
    private const val MIN_CONTENT_LENGTH = 15

    private val fillerTokens = setOf(
        "아", "어", "음", "응", "네", "예", "으", "흠",
        "uh", "um", "oh", "ah", "yeah", "yes"
    )

    fun canSummarize(text: String): Boolean {
        val normalized = text
            .replace(Regex("\\s+"), " ")
            .trim()

        if (normalized.length < MIN_CONTENT_LENGTH) return false

        val tokens = normalized
            .split(" ")
            .map { it.trim().trim('.', ',', '!', '?', '…').lowercase() }
            .filter { it.isNotBlank() }

        if (tokens.isEmpty()) return false
        return tokens.any { it !in fillerTokens }
    }
}
