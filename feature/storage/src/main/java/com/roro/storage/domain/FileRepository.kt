package com.roro.storage.domain

import com.roro.core.domain.model.FolderItem
import com.roro.core.domain.model.VoiceNoteItem
import com.roro.core.model.FolderWithNoteCount
import com.roro.core.model.Folder
import com.roro.core.model.VoiceNote
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface FileRepository {
    // 사용자 폴더 생성
    suspend fun createUserFolder(folderName: String): Boolean

    // voiceNote 생성
    suspend fun createVoiceNote(folderName: String?)

    // 폴더 휴지통으로 이동
    suspend fun moveToTrash(folder: Folder)

    // voiceNote 휴지통으로 이동
    suspend fun moveToVoiceNotes(voiceNoteIds: List<UUID>)

    // 폴더 휴지통 복원
    suspend fun restoreFolder(folderId: UUID)

    // voiceNote 휴지통 복원
    suspend fun restoreVoiceNote(voiceNoteId: UUID)

    // 폴더 리스트 fetch
    fun observeUserFolders(): Flow<List<Folder>>

    // 휴지통 리스트 fetch
    fun observeTrashFolders(): Flow<List<Folder>>

    // voiceNote fetch
    fun observeTrashVoiceNotes(): Flow<List<VoiceNote>>

    // FolderItem안에 개수 확인
    fun observeFolderItemCount(): Flow<List<FolderWithNoteCount>>

    // 휴지통 파일 개수 확인
    fun observeTrashFolderItemCount(): Flow<List<FolderWithNoteCount>>

    // 폴더가 없는 voiceNote fetch
    fun observeVoiceNotesByNullFolder(): Flow<List<VoiceNote>>

    // 폴더가 있는 voiceNote fetch
    fun observeVoiceNotesByNoneNullFolder(folderId: UUID): Flow<List<VoiceNote>>

    // 최근 voiceNote 5개
    fun observeRecentVoiceNote(): Flow<List<VoiceNote>>

    // voiceNote 영구삭제
    suspend fun removeVoiceNote(voiceNoteId: UUID)

    // folder 영구삭제
    suspend fun removeFolder(folderId: UUID)

    // 폴더 이름 변경
    suspend fun renameFolder(folder: Folder)

    // VoiceNote 이름 변경
    suspend fun renameVoiceNote(voiceNote: VoiceNote)

    /*      검색 선언       */
    // 홈 검색
    suspend fun searchFolders(query: String): List<FolderItem>
    suspend fun searchVoiceNotes(query: String): List<VoiceNoteItem>

    // 휴지통 검색
    suspend fun searchTrashFolder(query: String): List<FolderItem>
    suspend fun searchTrashVoiceNotes(query: String): List<VoiceNoteItem>


}