package com.roro.storage.data.repository

import com.roro.core.model.FolderWithNoteCount
import com.roro.core.mapper.toEntity
import com.roro.core.model.Folder
import com.roro.core.model.Keyword
import com.roro.core.model.Summary
import com.roro.core.model.Transcript
import com.roro.core.model.VoiceNote
import com.roro.core.model.VoiceRecord
import com.roro.storage.data.datasource.LocalFileDataSource
import com.roro.storage.data.datasource.RoomFileDataSource
import com.roro.storage.domain.FileRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import kotlin.String

class FileRepositoryImpl @Inject constructor(
    private val local: LocalFileDataSource,
    private val room: RoomFileDataSource
) : FileRepository {
    // 사용자 폴더 생성
    override suspend fun createUserFolder(folderName: String): Boolean {
        return room.insertFolder(
            Folder(
                name = folderName
            ).toEntity()
        )
    }

    // voiceNote 음성 파일 저장
    override suspend fun createVoiceNote(folderName: String?) {
        val file = local.createVoiceNote(folderName = folderName)
        val now = System.currentTimeMillis()
        val voiceNoteId = UUID.randomUUID()
        val tempFolder = if (folderName == "123") {
            "87e2dd57-be8a-4174-80fb-4c464924f546"
        } else {
            "62b7d63a-2fc7-4d90-8def-7181246d8260"
        }

        val voiceNote = VoiceNote(
            id = voiceNoteId,
            title = now.toString(),
            createdAt = now,
            updatedAt = now,
            folderId = if (folderName.isNullOrBlank()) {
                null
            } else {
                UUID.fromString(tempFolder)
            }
        )

        val voiceRecord = VoiceRecord(
            id = UUID.randomUUID(),
            audioFilePath = file.absolutePath,
            duration = 0.0,
            createdAt = now,
            voiceNoteId = voiceNoteId
        )

        val transcript = Transcript(
            id = UUID.randomUUID(),
            text = "임시 전사문",
            voiceNoteId = voiceNoteId

        )

        val summary = Summary(
            id = UUID.randomUUID(),
            text = "임시 요약문",
            voiceNoteId = voiceNoteId

        )

        val keywords = listOf(
            Keyword(
                id = UUID.randomUUID(),
                word = "회의",
                voiceNoteId = voiceNoteId

            ),
            Keyword(
                id = UUID.randomUUID(),
                word = "메모",
                voiceNoteId = voiceNoteId
            )
        )

        room.createVoiceNote(
            voiceNote = voiceNote.toEntity(),
            voiceRecord = voiceRecord.toEntity(),
            transcript = transcript.toEntity(),
            summary = summary.toEntity(),
            keywords = keywords.map
            { it.toEntity() }
        )

    }

    // 사용자 휴지통 이동 (폴더)
    override suspend fun moveToTrash(folder: Folder) {
        val now = System.currentTimeMillis()
        room.moveFolderWithVoiceNotesToTrash(
            folder = folder.copy(
                deletedAt = now,
                updatedAt = now
            ).toEntity()
        )
    }

    override suspend fun moveToVoiceNotes(voiceNoteIds: List<UUID>) {
        room.moveToTrashVoiceNotes(voiceNoteIds)
    }

    override suspend fun restoreFromTrash(folder: Folder) {
        room.restoreFolder(
            folder = folder.copy(
                deletedAt = null,
                updatedAt = System.currentTimeMillis()
            ).toEntity()
        )
    }

    override suspend fun restoreVoiceNote(voiceNote: VoiceNote) {
        room.restoreVoiceNote(
            voiceNote = voiceNote
        )
    }

    // 사용자 폴더 가져오기
    override fun observeUserFolders(): Flow<List<Folder>> {
        return room.observeUserFolder()
    }

    // 휴지통 폴더 가져오기
    override fun observeTrashFolders(): Flow<List<Folder>> {
        return room.observeTrashFolders()
    }

    override fun observeTrashVoiceNotes(): Flow<List<VoiceNote>> {
        return room.observeTrashVoiceNotes()
    }

    // 폴더가 가지고 있는 아이템 개수
    override fun observeFolderItemCount(): Flow<List<FolderWithNoteCount>> {
        return room.observeFolderItemCount()
    }

    // 폴더를 갖고 있지 않는 voiceNote조회
    override fun observeVoiceNotesByNullFolder(): Flow<List<VoiceNote>> {
        return room.observeFolderNullVoiceNote()
    }

    // 폴더가 있는 voiceNote 조회
    override fun observeVoiceNotesByNoneNullFolder(uuid: UUID): Flow<List<VoiceNote>> {
        return room.observeNotNullVoiceNote(folderId = uuid)
    }

    // 최근 voiceNote 5개
    override fun observeRecentVoiceNote(): Flow<List<VoiceNote>> {
        return room.observeRecentVoiceNote()
    }

    override suspend fun removeVoiceNote(voiceNote: VoiceNote) {
        room.removeVoiceNote(
            voiceNoteEntity = voiceNote.copy(
                id = voiceNote.id,
                title = voiceNote.title,
            ).toEntity()
        )
    }

    override suspend fun removeFolder(folder: Folder) {
        room.removeFolder(
            folder = folder.copy(
                id = folder.id,
                name = folder.name
            ).toEntity()
        )
    }

    override suspend fun renameFolder(folder: Folder) {
        room.renameFolder(
            folder = folder.copy(
                id = folder.id,
                name = folder.name,
                updatedAt = System.currentTimeMillis()
            ).toEntity()
        )
    }

    override suspend fun renameVoiceNote(voiceNote: VoiceNote) {
        room.renameVoiceNote(
            voiceNote = voiceNote.copy(
                id = voiceNote.id,
                title = voiceNote.title,
                updatedAt = System.currentTimeMillis()
            ).toEntity()
        )
    }
}