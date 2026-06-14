package com.roro.core.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.roro.core.domain.model.SummaryStatus
import java.util.UUID

/**
 * 기능 설명:
 * - 사용자가 생성한 음성 메모(노트) 정보를 저장하는 테이블.
 *
 * 컬럼 설명:
 * @property id         : 음성 메모의 고유 ID (Primary Key)
 * @property title      : 음성 메모 내용
 * @property createdAt  : 생성 시간
 * @property updatedAt  : 수정 시간
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */

@Entity(
    tableName = "voice_note",
    indices = [
        Index(value = ["folderId"]),
        Index(value = ["deletedAt"])
    ]
)
data class VoiceNoteEntity(
    @PrimaryKey val id: UUID,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
    val folderId: UUID?,

    val summaryStatus: String = SummaryStatus.NONE.name
)