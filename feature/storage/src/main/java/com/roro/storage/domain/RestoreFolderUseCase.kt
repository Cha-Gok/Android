package com.roro.storage.domain

import java.util.UUID
import javax.inject.Inject

class RestoreFolderUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend operator fun invoke(folderId: UUID) {
        repository.restoreFolder(folderId = folderId)
    }
}