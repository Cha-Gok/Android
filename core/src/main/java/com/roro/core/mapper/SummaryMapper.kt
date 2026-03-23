package com.roro.core.mapper

import com.roro.core.entity.SummaryEntity
import com.roro.core.entity.VoiceRecordEntity
import com.roro.core.model.Summary
import com.roro.core.model.VoiceRecord

fun SummaryEntity.toModel(): Summary {
    return Summary(
        id = id,
        text = text,
        createdAt = createdAt,
        voiceNoteId = voiceNoteId
    )
}

fun Summary.toEntity(): SummaryEntity {
    return SummaryEntity(
        id = id,
        text = text,
        createdAt = createdAt,
        voiceNoteId = voiceNoteId
    )
}