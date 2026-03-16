package com.roro.storage.domain

import com.roro.core.model.Folder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUserFoldersUseCase @Inject constructor(
    private val repository: FileRepository
) {
    operator fun invoke(): Flow<List<Folder>> {
        return repository.observeUserFolders()
    }
}