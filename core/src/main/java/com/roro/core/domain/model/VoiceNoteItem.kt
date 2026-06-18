package com.roro.core.domain.model

data class VoiceNoteItem(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val duration: String,
    val summaryStatus: SummaryStatus,
    val folderName: String
)
