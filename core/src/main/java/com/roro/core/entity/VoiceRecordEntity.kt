package com.roro.core.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 기능 설명:
 * - 실제 녹음된 오디오 파일 정보를 저장하는 테이블.
 *
 * 관계:
 * - VoiceNoteEntity (1) : VoiceRecordEntity (1)
 *
 * 컬럼 설명:
 * @property id            : 녹음 데이터의 고유 ID
 * @property voiceNoteId   : 해당 녹음이 속한 VoiceNote ID
 * @property audioPath     : 저장된 오디오 파일 경로
 * @property durationSec   : 녹음 길이
 * @property createdAt     : 생성 시간
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
@Entity(
    tableName = "voice_record",
    indices = [Index("voiceNoteId")],
    foreignKeys = [
        ForeignKey(
            entity = VoiceNoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["voiceNoteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class VoiceRecordEntity(
    @PrimaryKey val id: UUID,
    val voiceNoteId: UUID,
    val audioPath: String,
    val durationSec: Double = 0.0,
    val createdAt: Long
)