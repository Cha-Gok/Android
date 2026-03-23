package com.roro.storage.domain

import com.roro.core.model.FolderWithNoteCount
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFolderItemCount @Inject constructor(
    private val repository: FileRepository
) {
    operator fun invoke(): Flow<List<FolderWithNoteCount>> {
        return repository.observeFolderItemCount()
    }
}
