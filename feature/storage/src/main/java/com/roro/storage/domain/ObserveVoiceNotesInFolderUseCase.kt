package com.roro.storage.domain

import com.roro.core.domain.model.VoiceNoteItem
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class ObserveVoiceNotesInFolderUseCase @Inject constructor(
    private val repository: FileRepository
) {
    operator fun invoke(uuid: UUID): Flow<List<VoiceNoteItem>> {
        return repository.observeVoiceNotesByNoneNullFolder(folderId = uuid)
    }
}