package com.roro.recorder.domain.usecase

import com.roro.core.domain.model.FolderItem
import com.roro.recorder.domain.repository.RecordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFolders @Inject constructor(
    private val repository: RecordRepository
) {
    operator fun invoke(): Flow<List<FolderItem>> {
        return repository.observeFolders()
    }
}