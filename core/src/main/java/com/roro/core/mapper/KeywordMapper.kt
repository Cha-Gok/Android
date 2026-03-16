package com.roro.core.mapper

import com.roro.core.entity.KeywordEntity
import com.roro.core.model.Keyword

fun KeywordEntity.toModel(): Keyword {
    return Keyword(
        id = id,
        word = word,
        voiceNoteId = voiceNoteId
    )
}

fun Keyword.toEntity(): KeywordEntity {
    return KeywordEntity(
        id = id,
        word = word,
        voiceNoteId = voiceNoteId
    )
}