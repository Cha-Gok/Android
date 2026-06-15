package com.roro.recorder.domain.usecase

import com.roro.recorder.domain.repository.RecordRepository
import javax.inject.Inject

// 기본 폴더
class CreateUserFolderUseCase @Inject constructor(
    private val repository: RecordRepository
) {
    suspend operator fun invoke(folderName: String): Boolean {
        return repository.createUserFolder(folderName = folderName)
    }
}