package com.roro.core.domain.model

import java.util.UUID

data class VoiceNoteItemResult(
    val id: UUID,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val duration: Double?,
    val summaryStatus: SummaryStatus,
    val folderName: String?
)