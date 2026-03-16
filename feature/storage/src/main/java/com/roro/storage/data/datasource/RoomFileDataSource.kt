package com.roro.storage.data.datasource

import com.roro.core.dao.FolderDao
import com.roro.core.dao.KeywordDao
import com.roro.core.dao.SummaryDao
import com.roro.core.dao.TranscriptDao
import com.roro.core.dao.VoiceNoteDao
import com.roro.core.dao.VoiceRecordDao
import com.roro.core.entity.FolderEntity
import com.roro.core.entity.KeywordEntity
import com.roro.core.entity.SummaryEntity
import com.roro.core.entity.TranscriptEntity
import com.roro.core.entity.VoiceNoteEntity
import com.roro.core.entity.VoiceRecordEntity
import com.roro.core.model.FolderWithNoteCount
import com.roro.core.mapper.toModel
import com.roro.core.model.Folder
import com.roro.core.model.VoiceNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class RoomFileDataSource @Inject constructor(
    private val folderDao: FolderDao,
    private val voiceNoteDao: VoiceNoteDao,
    private val voiceRecordDao: VoiceRecordDao,
    private val transcriptDao: TranscriptDao,
    private val summaryDao: SummaryDao,
    private val keywordDao: KeywordDao
) {
    // 폴더 생성
    suspend fun insertFolder(folder: FolderEntity): Boolean {
        return try {
            folderDao.insertFolder(folder)
            true
        } catch (e: Exception) {
            false
        }
    }

    // 폴더 전체 업데이트
    suspend fun updateFolder(folder: FolderEntity) {
        folderDao.updateFolder(folder)
    }

    // 폴더를 휴지통으로 이동
    suspend fun moveToTrash(folder: FolderEntity) {
        val now = System.currentTimeMillis()
        folderDao.updateFolder(
            folder.copy(
                deletedAt = now,
                updatedAt = now
            )
        )
    }

    // 폴더와 내부 VoiceNote 파일 휴지통 이동
    suspend fun moveFolderWithVoiceNotesToTrash(folder: FolderEntity) {
        val now = System.currentTimeMillis()

        folderDao.updateFolder(
            folder.copy(
                deletedAt = now,
                updatedAt = now
            )
        )

        voiceNoteDao.moveToTrashByFolderId(
            folderId = folder.id,
            deletedAt = now,
            updatedAt = now
        )
    }

    suspend fun moveToTrashVoiceNotes(noteIds: List<UUID>) {
        val now = System.currentTimeMillis()

        voiceNoteDao.moveNotesToTrash(
            noteIds = noteIds,
            deletedAt = now,
            updatedAt = now
        )
    }

    // 휴지통에서 폴더 복원
    suspend fun restoreFolder(folder: FolderEntity) {
        val now = System.currentTimeMillis()
        folderDao.updateFolder(
            folder.copy(
                deletedAt = null,
                updatedAt = now
            )
        )
        voiceNoteDao.restoreByFolderId(
            folder.id,
            updatedAt = now
        )
    }

    // 단일 폴더 조회
    suspend fun getFolder(id: UUID): Folder? {
        return folderDao.getFolder(id)?.toModel()
    }

    // 사용자 폴더 조회
    fun observeUserFolder(): Flow<List<Folder>> {
        return folderDao.observeUserFolders().map { list ->
            list.map { it.toModel() }
        }
    }

    // VoiceNote 생성 임시
    suspend fun createVoiceNote(
        voiceNote: VoiceNoteEntity,
        voiceRecord: VoiceRecordEntity,
        transcript: TranscriptEntity,
        summary: SummaryEntity,
        keywords: List<KeywordEntity>
    ) {
        voiceNoteDao.insert(voiceNote)
        voiceRecordDao.insert(voiceRecord)
        transcriptDao.insert(transcript)
        summaryDao.insert(summary)
        keywordDao.insertAll(keywords)
    }

    // 휴지통 폴더 목록 조회
    fun observeTrashFolders(): Flow<List<Folder>> {
        return folderDao.observeTrashFolders().map { list ->
            list.map { it.toModel() }
        }
    }

    // 각 폴더별 아이템 개수
    fun observeFolderItemCount(): Flow<List<FolderWithNoteCount>> {
        return voiceNoteDao.observeFolderNoteCount()
    }

    // 폴더를 가지고 있는 voiceNote 조회
    fun observeNotNullVoiceNote(folderId: UUID): Flow<List<VoiceNote>> {
        return voiceNoteDao.observeVoiceNote(folderId)
            .map { list -> list.map { it.toModel() } }
    }

    // 폴더가 없는 voiceNote 조회
    fun observeFolderNullVoiceNote(): Flow<List<VoiceNote>> {
        return voiceNoteDao.observeFolderNullVoiceNote()
            .map { list -> list.map { it.toModel() } }
    }

    // 폴더 이름 변경
    suspend fun renameFolder(
        folderId: UUID,
        name: String,
        updatedAt: Long
    ) {
        folderDao.renameFolder(folderId, name, updatedAt)
    }

    // 폴더 완전 삭제
    suspend fun deleteFolder(folder: FolderEntity) {
        folderDao.deleteFolder(folder)
    }
}