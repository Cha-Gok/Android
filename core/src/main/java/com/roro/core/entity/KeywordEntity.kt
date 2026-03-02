package com.roro.core.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * 기능 설명:
 * - 음성 메모(VoiceNote)에서 추출된 키워드를 저장하는 테이블
 * - 하나의 VoiceNote에 여러 개의 키워드가 연결될 수 있다.
 *
 * 관계:
 * - VoiceNoteEntity (1) : KeywordEntity (N)
 * - VoiceNote 삭제 시 Keyword도 함께 삭제됨(CASCADE)
 *
 * 컬럼 설명:
 * @property id             : 키워드 고유 ID (Primary Key)
 * @property voiceNoteId    : voiceNoteId 키워드가 속한 VoiceNote ID (Foreign Key)
 * @property word           : 추출된 키워드
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
@Entity(
    tableName = "keyword",
    indices = [Index("voiceNoteId"), Index("word")],
    foreignKeys = [
        ForeignKey(
            entity = VoiceNoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["voiceNoteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],

)
data class KeywordEntity(
    @PrimaryKey val id: UUID,
    val voiceNoteId: UUID,
    val word: String
)