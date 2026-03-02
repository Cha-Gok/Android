package com.roro.core.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 기능 설명:
 * - 음성 메모의 AI 요약 결과를 저장하는 테이블.
 *
 * 관계:
 * - VoiceNoteEntity (1) : SummaryEntity (1)
 *
 * 컬럼 설명:
 * @property id          : 요약 데이터의 고유 ID
 * @property voiceNoteId : 해당 요약이 속한 VoiceNote ID
 * @property text        : 생성된 요약 텍스트
 * @property createdAt   : 생성 시간
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
@Entity(
    tableName = "summary",
    indices = [Index("voiceNoteId", unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = VoiceNoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["voiceNoteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SummaryEntity(
    @PrimaryKey val id: UUID,
    val voiceNoteId: UUID,
    val text: String,
    val createdAt: Long
)