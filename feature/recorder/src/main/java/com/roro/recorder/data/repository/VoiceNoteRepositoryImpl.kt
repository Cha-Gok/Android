package com.roro.recorder.data.repository

import com.roro.core.dao.VoiceNoteDao
import com.roro.core.mapper.toDomain
import com.roro.core.mapper.toEntity
import com.roro.core.model.VoiceNote
import com.roro.recorder.domain.repository.VoiceNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

// Room DAO를 domain으로 변환해주는 역할
// "어떻게 할 것인지"를 실제로 구현
class VoiceNoteRepositoryImpl(
    private val dao: VoiceNoteDao
) : VoiceNoteRepository {

    override suspend fun createVoiceNote(title: String): VoiceNote {
        val note = VoiceNote(
            id = UUID.randomUUID(),
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        dao.upsert(note.toEntity())
        return note
    }

    override fun observeVoiceNotes(): Flow<List<VoiceNote>> =
        dao.observeNotes().map { list -> list.map { it.toDomain() } }

    override suspend fun getVoiceNote(id: UUID): VoiceNote? =
        dao.getNote(id)?.toDomain()

    override suspend fun deleteVoiceNote(id: UUID) {
        val entity = dao.getNote(id) ?: return
        dao.delete(entity)
    }

    override suspend fun updateVoiceNote(note: VoiceNote) {
        dao.upsert(note.toEntity().copy(updatedAt = System.currentTimeMillis()))
    }
}