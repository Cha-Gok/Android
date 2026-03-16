package com.roro.core.model

import java.util.UUID

/**
 * 기능 설명: Summary 데이터 클래스
 *
 * Room 사용으로 타입 변경
 * createdAt: Date() -> Long
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
data class Summary(
    val id: UUID = UUID.randomUUID(),
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
    val voiceNoteId: UUID,
)
