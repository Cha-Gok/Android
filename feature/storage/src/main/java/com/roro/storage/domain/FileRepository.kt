package com.roro.storage.domain

import com.roro.core.model.FolderWithNoteCount
import com.roro.core.model.Folder
import com.roro.core.model.VoiceNote
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface FileRepository {
    suspend fun createUserFolder(folderName: String): Boolean
    suspend fun createVoiceNote(folderName: String?)
    suspend fun moveToTrash(folder: Folder)
    suspend fun moveToVoiceNotes(voiceNoteIds: List<UUID>)
    suspend fun restoreFromTrash(folder: Folder)
    fun observeUserFolders(): Flow<List<Folder>>
    fun observeTrashFolders(): Flow<List<Folder>>
    fun observeFolderItemCount(): Flow<List<FolderWithNoteCount>>
    fun observeVoiceNotesByNullFolder(): Flow<List<VoiceNote>>
    fun observeVoiceNotesByNoneNullFolder(uuid: UUID): Flow<List<VoiceNote>>
}