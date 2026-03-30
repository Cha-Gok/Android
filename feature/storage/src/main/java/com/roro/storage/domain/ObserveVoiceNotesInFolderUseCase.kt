package com.roro.storage.domain

import com.roro.core.model.VoiceNote
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class ObserveVoiceNotesInFolderUseCase @Inject constructor(
    private val repository: FileRepository
) {
    operator fun invoke(uuid: UUID): Flow<List<VoiceNote>> {
        return repository.observeVoiceNotesByNoneNullFolder(uuid = uuid)
    }
}