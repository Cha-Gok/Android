package com.roro.storage.domain

import javax.inject.Inject

// 기본 폴더
class CreateVoiceNoteUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend operator fun invoke(folderName:String?) {
        return repository.createVoiceNote(folderName)
    }
}