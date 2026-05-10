package com.roro.core.domain.model

import java.util.UUID

data class TrashItem(
    val id: UUID,
    val title: String,
    val deletedAt: Long,
    val firstText: String,
    val secondText: String,
    val type: TrashType
)

enum class TrashType { FOLDER, VOICE_NOTE }