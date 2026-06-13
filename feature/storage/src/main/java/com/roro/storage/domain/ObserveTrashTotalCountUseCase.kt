package com.roro.storage.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTrashTotalCountUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend operator fun invoke(): Flow<Int> {
        return repository.observeTrashTotalCount()
    }
}