package com.roro.core.model

import com.roro.core.domain.model.SummaryStatus
import java.util.UUID

/**
 * 기능 설명: VoiceNote 데이터 클래스
 *
 * Room 사용으로 타입 변경
 * createdAt: Date() -> Long
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
data class VoiceNote(
    val id: UUID,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt,
    val deletedAt: Long? = null,
    val folderId: UUID? = null,

    val summaryStatus: SummaryStatus = SummaryStatus.NONE
)