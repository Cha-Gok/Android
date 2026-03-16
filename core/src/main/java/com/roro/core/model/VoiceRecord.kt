package com.roro.core.model

import java.util.UUID

/**
 * 기능 설명: VoiceNote 데이터 클래스
 *
 * Room 사용으로 타입 변경
 * path: Uri -> String
 * createdAt: Date() -> Long
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
data class VoiceRecord(
    val id: UUID = UUID.randomUUID(),
    val audioFilePath: String,
    val duration: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val voiceNoteId: UUID,
)
