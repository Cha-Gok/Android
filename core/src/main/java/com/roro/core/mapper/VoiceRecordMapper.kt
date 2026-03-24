package com.roro.core.mapper

import android.R.attr.duration
import com.roro.core.entity.VoiceRecordEntity
import com.roro.core.model.VoiceRecord

fun VoiceRecordEntity.toModel(): VoiceRecord {
    return VoiceRecord(
        id = id,
        audioFilePath = audioPath,
        createdAt = createdAt,
        voiceNoteId = voiceNoteId,
        duration = durationSec,
    )
}

fun VoiceRecord.toEntity(): VoiceRecordEntity {
    return VoiceRecordEntity(
        id = id,
        voiceNoteId = voiceNoteId,
        audioPath = audioFilePath,
        createdAt = createdAt,
        durationSec = duration,   // 🔥🔥🔥 이거 추가
    )
}