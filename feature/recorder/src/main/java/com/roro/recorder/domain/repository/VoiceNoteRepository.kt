package com.roro.recorder.domain.repository

import com.roro.core.model.VoiceNote
import kotlinx.coroutines.flow.Flow
import java.util.UUID

// "무엇을 할 수 있는지"만 정의. 실제 동작은 없다.
interface VoiceNoteRepository {

    suspend fun createVoiceNote(title: String): VoiceNote

    fun observeVoiceNotes(): Flow<List<VoiceNote>>

    suspend fun getVoiceNote(id: UUID): VoiceNote?

    suspend fun deleteVoiceNote(id: UUID)

    suspend fun updateVoiceNote(note: VoiceNote)
}