package com.roro.core.model

import java.util.UUID

/**
 * 기능 설명: Folder 데이터 클래스 정의
 *
 * Room 사용으로 타입 변경
 * path: Uri -> String
 * createdAt: Date() -> Long
 * updatedAt: Date() -> Long
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
data class Folder(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val deletedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt
)