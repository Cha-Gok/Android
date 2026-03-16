package com.roro.core.model

import java.util.UUID

/**
 * 기능 설명: Keyword 데이터 클래스
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
data class Keyword(
    val id: UUID = UUID.randomUUID(),
    val word: String,
    val voiceNoteId: UUID,
)
