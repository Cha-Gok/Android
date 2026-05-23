package com.roro.core.domain.model

import java.util.UUID

data class TrashItem(
    val id: UUID,
    val title: String,
    val deletedAt: Long,
    val firstText: String,
    val secondText: String,
    val type: FileType
)

enum class FileType { FOLDER, VOICE_NOTE }