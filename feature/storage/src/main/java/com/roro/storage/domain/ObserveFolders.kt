package com.roro.storage.domain

import com.roro.core.domain.model.FolderItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFolders @Inject constructor(
    private val repository: FileRepository
) {
    operator fun invoke() : Flow<List<FolderItem>>{
        return repository.observeFolders()
    }
}