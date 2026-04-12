package com.roro.storage.domain

import com.roro.core.model.Folder
import com.roro.core.model.VoiceNote
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTrashVoiceNotesUseCase @Inject constructor(
    private val repository: FileRepository
) {
    operator fun invoke(): Flow<List<VoiceNote>> {
        return repository.observeTrashVoiceNotes()
    }
}