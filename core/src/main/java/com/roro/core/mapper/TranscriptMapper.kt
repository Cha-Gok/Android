package com.roro.core.mapper

import com.roro.core.entity.SummaryEntity
import com.roro.core.entity.TranscriptEntity
import com.roro.core.entity.VoiceRecordEntity
import com.roro.core.model.Summary
import com.roro.core.model.Transcript
import com.roro.core.model.VoiceRecord

fun TranscriptEntity.toModel(): Transcript {
    return Transcript(
        id = id,
        text = text,
        createdAt = createdAt,
        voiceNoteId = voiceNoteId
    )
}

fun Transcript.toEntity(): TranscriptEntity {
    return TranscriptEntity(
        id = id,
        text = text,
        createdAt = createdAt,
        voiceNoteId = voiceNoteId
    )
}