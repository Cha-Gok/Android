package com.roro.core.domain.mapper

import com.roro.core.domain.model.VoiceNoteItem
import com.roro.core.domain.model.VoiceNoteItemResult
import com.roro.core.util.formatDate
import com.roro.core.util.toTimeFormat

fun VoiceNoteItemResult.toItem(): VoiceNoteItem {
    return VoiceNoteItem(
        id = this.id.toString(),
        title = this.title,
        duration = (this.duration ?: 0.0).toTimeFormat(),
        createAt = this.createAt.formatDate(),
        summary = this.summary ?: "",
        folderName = this.folderName ?: ""
    )
}