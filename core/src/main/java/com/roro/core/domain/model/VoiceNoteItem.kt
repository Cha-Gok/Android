package com.roro.core.domain.model

data class VoiceNoteItem(
    val id: String,
    val title: String,
    val createAt: String,
    val duration: String,
    val summary: String, // 이넘 변경 예정
    val folderName: String
)
