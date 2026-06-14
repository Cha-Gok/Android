package com.roro.core.mapper

import com.roro.core.domain.model.SummaryStatus
import com.roro.core.entity.VoiceNoteEntity
import com.roro.core.model.VoiceNote

fun VoiceNoteEntity.toModel(): VoiceNote {
    return VoiceNote(
        id = id,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        folderId = folderId,

        summaryStatus = SummaryStatus.valueOf(summaryStatus)
    )
}

fun VoiceNote.toEntity(): VoiceNoteEntity {
    return VoiceNoteEntity(
        id = id,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        folderId = folderId,
        summaryStatus = summaryStatus.name
    )
}