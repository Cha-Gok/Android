package com.roro.core.domain.model

data class HomeItem(
    val id: String,
    val title: String,
    val createAt: String,
    val duration: String,
    val state: String,
    val type: FileType
)

