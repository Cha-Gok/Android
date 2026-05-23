package com.roro.core.domain.model

import java.util.UUID


data class FolderItemResult(
    val id: UUID,
    val title: String,
    val count: Int,
    val createAt: Long
)