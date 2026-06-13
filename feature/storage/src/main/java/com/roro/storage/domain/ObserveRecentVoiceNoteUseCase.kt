package com.roro.storage.domain

import com.roro.core.domain.model.VoiceNoteItem
import com.roro.core.model.VoiceNote
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRecentVoiceNoteUseCase @Inject constructor(
    private val repository: FileRepository
) {
    operator fun invoke(): Flow<List<VoiceNoteItem>> {
        return repository.observeRecentVoiceNote()
    }
}