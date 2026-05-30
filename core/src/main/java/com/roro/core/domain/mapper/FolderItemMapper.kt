package com.roro.core.domain.mapper

import com.roro.core.domain.model.FolderItem
import com.roro.core.domain.model.FolderItemResult
import com.roro.core.util.formatDate

fun FolderItemResult.toItem(): FolderItem {
    return FolderItem(
        id = this.id.toString(),
        title = this.title,
        count = "${this.count}",
        createAt = this.createAt,
    )
}

fun List<FolderItemResult>.toItem(): List<FolderItem> {
    return this.map{it.toItem()}
}