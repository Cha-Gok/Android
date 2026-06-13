package com.roro.storage.domain

import com.roro.core.model.Folder
import java.util.UUID
import javax.inject.Inject

class MoveToTrashUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend operator fun invoke(folderId: UUID) {
        repository.moveToTrash(folderId = folderId)
    }
}