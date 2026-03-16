package com.roro.storage.domain

import javax.inject.Inject

// 기본 폴더
class CreateUserFolderUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend operator fun invoke(folderName: String): Boolean {
        return repository.createUserFolder(folderName = folderName)
    }
}