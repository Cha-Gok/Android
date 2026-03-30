package com.roro.storage.domain

import com.roro.core.model.Folder
import javax.inject.Inject

class RemoveFolderUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend operator fun invoke(folder: Folder) {
        repository.removeFolder(folder = folder)
    }
}