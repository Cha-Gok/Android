package com.roro.core.mapper

import com.roro.core.entity.VoiceNoteEntity
import com.roro.core.entity.VoiceRecordEntity
import com.roro.core.model.VoiceNote
import com.roro.core.model.VoiceRecord
import java.util.UUID

/**
 * 기능 설명:
 * Room(Entity) ↔ Domain(Model) 간 데이터 변환
 * 1. Entity → Domain 변환
 * 2. Domain → Entity 변환
 *
 * @author hyeonseo
 * @since 2026-03-20
 */

/**
 * Entity → Domain 변환
 * DB에서 가져온 VoiceNoteEntity를 앱에서 사용하는 VoiceNote로 변환한다.
 */
fun VoiceNoteEntity.toDomain(): VoiceNote {
    return VoiceNote(
        id = this.id,
        title = this.title,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

/**
 * Domain → Entity 변환
 * 앱에서 사용하는 VoiceNote를 DB에 저장할 VoiceNoteEntity로 변환한다.
 */
fun VoiceNote.toEntity(): VoiceNoteEntity {
    return VoiceNoteEntity(
        id = this.id,
        title = this.title,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

/**
 * Entity → Domain 변환
 * DB에서 가져온 VoiceRecordEntity를 앱에서 사용하는 VoiceRecord로 변환한다.
 */
fun VoiceRecordEntity.toDomain(): VoiceRecord {
    return VoiceRecord(
        id = this.id,
        voiceNoteId = this.voiceNoteId,
        audioFilePath = this.audioPath,
        duration = this.durationSec,
        createdAt = this.createdAt
    )
}

/**
 * Domain → Entity 변환
 * 앱에서 사용하는 VoiceRecord를 DB에 저장할 VoiceRecordEntity로 변환한다.
 */
fun VoiceRecord.toEntity(): VoiceRecordEntity {
    return VoiceRecordEntity(
        id = this.id,
        voiceNoteId = this.voiceNoteId,
        audioPath = this.audioFilePath,
        durationSec = this.duration,
        createdAt = this.createdAt
    )
}