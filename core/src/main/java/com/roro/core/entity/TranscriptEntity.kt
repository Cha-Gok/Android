package com.roro.core.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 기능 설명:
 * - 음성 녹음을 텍스트로 변환한 결과(STT)를 저장하는 테이블.
 *
 * 관계:
 * - VoiceNoteEntity (1) : TranscriptEntity (1)
 *
 * 컬럼 설명:
 * @property id          : 텍스트 변환 데이터 ID
 * @property voiceNoteId : 해당 텍스트가 속한 VoiceNote ID
 * @property text        : 변환된 전체 텍스트
 * @property createdAt   : 생성 시간
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
@Entity(
    tableName = "transcript",
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
data class TranscriptEntity(
    @PrimaryKey val id: UUID,
    val voiceNoteId: UUID,
    val text: String,
    val createdAt: Long
)