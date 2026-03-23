package com.roro.core.model

import java.util.UUID

data class FolderWithNoteCount(
    val id: UUID?,
    val name: String,
    val noteCount: Int
)