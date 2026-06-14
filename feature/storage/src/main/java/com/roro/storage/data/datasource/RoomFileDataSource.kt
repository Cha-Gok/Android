package com.roro.storage.data.datasource

import com.roro.core.dao.FolderDao
import com.roro.core.dao.KeywordDao
import com.roro.core.dao.SummaryDao
import com.roro.core.dao.TranscriptDao
import com.roro.core.dao.VoiceNoteDao
import com.roro.core.dao.VoiceRecordDao
import com.roro.core.domain.mapper.toItem
import com.roro.core.domain.model.FolderItem
import com.roro.core.domain.model.VoiceNoteItem
import com.roro.core.entity.FolderEntity
import com.roro.core.entity.KeywordEntity
import com.roro.core.entity.SummaryEntity
import com.roro.core.entity.TranscriptEntity
import com.roro.core.entity.VoiceNoteEntity
import com.roro.core.entity.VoiceRecordEntity
import com.roro.core.mapper.toModel
import com.roro.core.model.Folder
import com.roro.core.model.FolderWithNoteCount
import com.roro.core.model.VoiceNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import timber.log.Timber
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
    suspend fun moveFolderWithVoiceNotesToTrash(folderId: UUID) {
        val now = System.currentTimeMillis()

        val folder = folderDao.getFolder(folderId) ?: return

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
    suspend fun restoreFolder(folderId: UUID) {
        val now = System.currentTimeMillis()
        // 1. 폴더 자체를 복원 (deletedAt = null)
        folderDao.restoreFolder(folderId, now)
        // 2. 해당 폴더 안에 있던 모든 VoiceNote들도 같이 복원
        voiceNoteDao.restoreByFolderId(folderId, now)
    }

    // 원래 폴더가 있으면 해당 폴더로 복구, 없으면 루트로 복구
    suspend fun restoreVoiceNote(voiceNoteId: UUID) {
        val now = System.currentTimeMillis()

        // 1. 먼저 해당 VoiceNote의 정보를 DB에서 가져옴
        // (VoiceNoteEntity? 타입을 반환하도록 Dao가 수정되어 있어야 합니다)
        val voiceNote = voiceNoteDao.getNote(voiceNoteId) ?: return
        val folderId = voiceNote.folderId

        if (folderId == null) {
            // 처음부터 폴더가 없었던 경우 바로 복원
            voiceNoteDao.restoreVoiceNote(voiceNoteId, now)
            return
        }

        // 2. 부모 폴더가 여전히 존재하는지 확인
        // (deletedAt이 NULL인 정상 폴더만 가져옴)
        val folder = folderDao.getFolder(folderId)

        if (folder != null && folder.deletedAt == null) {
            // 부모 폴더가 존재하면 원래 위치로 복원
            voiceNoteDao.restoreVoiceNote(voiceNoteId, now)
        } else {
            // 부모 폴더가 이미 영구 삭제되었거나 휴지통에 있다면 루트(null)로 복원
            voiceNoteDao.restoreVoiceNoteToRoot(voiceNoteId, now)
        }
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

    // 휴지통 VoiceNotes 목록 조회
    fun observeTrashVoiceNotes(): Flow<List<VoiceNote>> {
        return voiceNoteDao.observeTrashVoiceNotes()
            .map { list -> list.map { it.toModel() } }
    }

    // 각 폴더별 아이템 개수
    fun observeFolderItemCount(): Flow<List<FolderWithNoteCount>> {
        return voiceNoteDao.observeFolderNoteCount()
    }

    // 각 휴지통 아이템 개수
    fun observeTrashFolderItemCount(): Flow<List<FolderWithNoteCount>> {
        return voiceNoteDao.observeTrashFolderNoteCount()
    }

    // 폴더를 가지고 있는 voiceNote 조회
    fun observeNotNullVoiceNote(folderId: UUID): Flow<List<VoiceNoteItem>> {
        return voiceNoteDao.observeVoiceNoteInFolder(folderId).map { it.toItem() }
    }

    // 폴더가 없는 voiceNote 조회
    fun observeFolderNullVoiceNote(): Flow<List<VoiceNote>> {
        return voiceNoteDao.observeFolderNullVoiceNote()
            .map { list -> list.map { it.toModel() } }
    }

    // 최근 voiceNote 5개 조회
    fun observeRecentVoiceNote(): Flow<List<VoiceNoteItem>> {
        return voiceNoteDao.observeRecentVoiceNote().map { result ->
            result.toItem()
        }
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
    suspend fun removeFolder(folderId: UUID) {
        folderDao.removeFolder(folderId)
        voiceNoteDao.removeVoiceNotesByFolderId(folderId)
    }

    // voiceNote 제거
    suspend fun removeVoiceNote(voiceNoteId: UUID) {
        voiceNoteDao.removeVoiceNote(voiceNoteId)
    }

    // 폴더 이름 변경
    suspend fun renameFolder(folder: FolderEntity) {
        folderDao.renameFolder(
            folderId = folder.id,
            name = folder.name,
            updatedAt = folder.updatedAt
        )
    }

    // VoiceNote 이름 변경
    suspend fun renameVoiceNote(voiceNote: VoiceNoteEntity) {
        voiceNoteDao.renameVoiceNote(
            noteId = voiceNote.id,
            voiceNoteTitle = voiceNote.title,
            updatedAt = voiceNote.updatedAt
        )
    }

    /*          홈 화면            */
    fun observeTrashTotalCount(): Flow<Int> {
        return voiceNoteDao.observeCountTrashRootVoiceNotes()
    }

    /*          폴더 가져오기       */
    fun observeFolders(): Flow<List<FolderItem>> {
        return folderDao.observeFolders().map { it.toItem() }
    }

    // 기본 폴더 voiceNote 가져오기
    fun observeVoiceNote(): Flow<List<VoiceNoteItem>> {
        return voiceNoteDao.observeRootVoiceNotes().map { results ->
            results.toItem()
        }
    }

    /*          검색 로직          */

    // 홈 검색
    suspend fun searchFolders(query: String): List<FolderItem> {
        return folderDao.searchFolders(query = query).map { result ->
            result.toItem()
        }
    }

    suspend fun moveToFolder(voiceNoteId: List<UUID>, folderId: String) {
        val now = System.currentTimeMillis()
        voiceNoteDao.moveToFolder(
            voiceNoteId = voiceNoteId,
            folderId = folderId,
            updatedAt = now
        )
    }


    // 홈 검색
    suspend fun searchVoiceNotes(query: String): List<VoiceNoteItem> {
        return voiceNoteDao.searchVoiceNotes(query = query).map { result ->
            result.toItem()
        }
    }

    // 파일리스트 검색
    suspend fun searchVoiceNoteInFolder(query: String, folderId: String): List<VoiceNoteItem> {
        return voiceNoteDao.searchVoiceNoteInFolder(query = query, folderId = folderId).toItem()
    }

    // 휴지통 내 폴더 검색
    suspend fun searchTrashFolders(query: String): List<FolderItem> {
        return folderDao.searchTrashFolders(query).toItem()
    }

    // 휴지통 내 VoiceNote 검색
    suspend fun searchTrashVoiceNotes(query: String): List<VoiceNoteItem> {
        return voiceNoteDao.searchTrashVoiceNotes(query).map { result ->
            result.toItem()
        }
    }

}