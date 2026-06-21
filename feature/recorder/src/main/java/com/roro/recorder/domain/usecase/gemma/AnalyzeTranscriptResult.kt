package com.roro.recorder.domain.usecase.gemma

data class AnalyzeTranscriptResult(
    val summaryText: String,
    val keywords: List<String>
)
