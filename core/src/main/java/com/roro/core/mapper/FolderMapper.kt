package com.roro.core.mapper

import com.roro.core.entity.FolderEntity
import com.roro.core.model.Folder

fun FolderEntity.toModel(): Folder {
    return Folder(
        id = id,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt
    )
}

fun Folder.toEntity(): FolderEntity {
    return FolderEntity(
        id = id,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )
}