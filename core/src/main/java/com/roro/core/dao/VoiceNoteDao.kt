package com.roro.core.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.roro.core.domain.model.VoiceNoteItemResult
import com.roro.core.entity.VoiceNoteEntity
import com.roro.core.model.FolderWithNoteCount
import com.roro.core.model.VoiceNote
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * 기능 설명:
 * - VoiceNoteDao 테이블에 대한 데이터 접근을 담당한다.
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
@Dao
interface VoiceNoteDao {

    @Query("SELECT * FROM voice_note ORDER BY updatedAt DESC")
    fun observeNotes(): Flow<List<VoiceNoteEntity>>

    @Query("SELECT * FROM voice_note WHERE id = :id LIMIT 1")
    suspend fun getNote(id: UUID): VoiceNoteEntity?

    @Query(
        """
        SELECT f.id, f.name, COUNT(v.id) as noteCount
        FROM folder f
        LEFT JOIN voice_note v ON f.id = v.folderId AND v.deletedAt IS NULL
        WHERE f.deletedAt IS NULL
        GROUP BY f.id
    """
    )
    fun observeFolderNoteCount(): Flow<List<FolderWithNoteCount>>

    @Query(
        """
        SELECT f.id, f.name, COUNT(v.id) as noteCount
        FROM folder f
        LEFT JOIN voice_note v ON f.id = v.folderId 
        WHERE f.deletedAt IS NOT NULL
        GROUP BY f.id
    """
    )
    fun observeTrashFolderNoteCount(): Flow<List<FolderWithNoteCount>>

    // 특정 폴더에 속한 정상 VoiceNote 조회 (조인 추가)
    @Query(
        """
        SELECT 
            vn.id as id,
            vn.title as title,
            vn.createdAt as createdAt,
            vn.updatedAt as updatedAt,
            vr.durationSec as duration,
            s.text as summary,
            f.name as folderName
        FROM voice_note vn
        LEFT JOIN voice_record vr ON vn.id = vr.voiceNoteId
        LEFT JOIN summary s ON vn.id = s.voiceNoteId
        LEFT JOIN folder f ON vn.folderId = f.id
        WHERE vn.folderId = :folderId AND vn.deletedAt IS NULL
        ORDER BY vn.createdAt DESC
        """
    )
    fun observeVoiceNoteInFolder(folderId: UUID): Flow<List<VoiceNoteItemResult>>

    // 폴더가 없는(루트) 정상 VoiceNote 조회 (조인 포함)
    @Query(
        """
        SELECT 
            vn.id as id,
            vn.title as title,
            vn.createdAt as createdAt,
            vn.updatedAt as updatedAt,
            vr.durationSec as duration,
            s.text as summary,
            f.name as folderName
        FROM voice_note vn
        LEFT JOIN voice_record vr ON vn.id = vr.voiceNoteId
        LEFT JOIN summary s ON vn.id = s.voiceNoteId
        LEFT JOIN folder f ON vn.folderId = f.id
        WHERE vn.folderId IS NULL AND vn.deletedAt IS NULL
        ORDER BY vn.createdAt DESC
        """
    )
    fun observeRootVoiceNotes(): Flow<List<VoiceNoteItemResult>>

    // 폴더가 없는(루트) 정상 VoiceNote 조회
    @Query("SELECT * FROM voice_note WHERE folderId IS NULL AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun observeFolderNullVoiceNote(): Flow<List<VoiceNoteEntity>>

    // 휴지통에 있는 VoiceNote 조회 (부모 폴더가 삭제되었거나 파일 자체가 삭제된 경우)
    @Query(
        """
        SELECT vn.* FROM voice_note vn
        LEFT JOIN folder f ON vn.folderId = f.id
        WHERE vn.deletedAt IS NOT NULL
        AND (vn.folderId IS NULL OR f.deletedAt IS NULL)
        ORDER BY vn.deletedAt DESC
    """
    )
    fun observeTrashVoiceNotes(): Flow<List<VoiceNoteEntity>>

    // 최근 업데이트된 VoiceNote 상위 5개
    @Query("SELECT * FROM voice_note WHERE deletedAt IS NULL ORDER BY updatedAt DESC LIMIT 5")
    fun observeRecentVoiceNote(): Flow<List<VoiceNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(note: VoiceNoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: VoiceNoteEntity)

    // --- 휴지통 이동 (Soft Delete) ---

    @Query("UPDATE voice_note SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE folderId = :folderId AND deletedAt IS NULL")
    suspend fun moveToTrashByFolderId(folderId: UUID, deletedAt: Long, updatedAt: Long)

    @Query("UPDATE voice_note SET deletedAt = :deletedAt, updatedAt = :updatedAt WHERE id IN (:noteIds)")
    suspend fun moveNotesToTrash(noteIds: List<UUID>, deletedAt: Long, updatedAt: Long)

    // --- 복원 (Restore) ---

    // 폴더 복원 시 내부 파일들 일괄 복원
    @Query("UPDATE voice_note SET deletedAt = NULL, updatedAt = :updatedAt WHERE folderId = :folderId")
    suspend fun restoreByFolderId(folderId: UUID, updatedAt: Long)

    // 개별 파일 복원 (기존 폴더 유지)
    @Query("UPDATE voice_note SET deletedAt = NULL, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun restoreVoiceNote(noteId: UUID, updatedAt: Long)

    // 개별 파일 복원 (폴더를 루트로 변경)
    @Query("UPDATE voice_note SET folderId = NULL, deletedAt = NULL, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun restoreVoiceNoteToRoot(noteId: UUID, updatedAt: Long)

    // --- 영구 삭제 (Hard Delete) ---

    // 개별 파일 영구 삭제
    @Query("DELETE FROM voice_note WHERE id = :voiceNoteId")
    suspend fun removeVoiceNote(voiceNoteId: UUID)

    // 특정 폴더 내 모든 파일 영구 삭제
    @Query("DELETE FROM voice_note WHERE folderId = :folderId")
    suspend fun removeVoiceNotesByFolderId(folderId: UUID)

    // --- 기타 수정 ---

    @Query("UPDATE voice_note SET title = :voiceNoteTitle, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun renameVoiceNote(noteId: UUID, voiceNoteTitle: String, updatedAt: Long)

    // 전체 음성 메모 검색 (정상 상태, 폴더명 포함)
    @Query(
        """
        SELECT 
            vn.id as id,
            vn.title as title,
            vn.createdAt as createdAt,
            vn.updatedAt as updatedAt,
            vr.durationSec as duration,
            s.text as summary,
            f.name as folderName
        FROM voice_note vn
        LEFT JOIN voice_record vr ON vn.id = vr.voiceNoteId
        LEFT JOIN summary s ON vn.id = s.voiceNoteId
        LEFT JOIN folder f ON vn.folderId = f.id
        WHERE vn.deletedAt IS NULL -- 👈 정상 상태인 파일만 필터링
        AND vn.title LIKE '%' || :query || '%'
        ORDER BY vn.createdAt DESC
        """
    )
    suspend fun searchVoiceNotes(query: String): List<VoiceNoteItemResult>


    // 휴지통 검색 (폴더명 포함)
    @Query(
        """
    SELECT 
        vn.id as id,
        vn.title as title,
        vn.createdAt as createdAt,
        vn.updatedAt as updatedAt,
        vr.durationSec as duration,
        s.text as summary,
        f.name as folderName -- 👈 폴더 테이블의 이름을 가져옴
    FROM voice_note vn
    LEFT JOIN voice_record vr ON vn.id = vr.voiceNoteId
    LEFT JOIN summary s ON vn.id = s.voiceNoteId
    LEFT JOIN folder f ON vn.folderId = f.id -- 👈 폴더 테이블 조인 추가
    WHERE vn.deletedAt IS NOT NULL 
    AND vn.title LIKE '%' || :query || '%'
    ORDER BY vn.deletedAt DESC
"""
    )
    suspend fun searchTrashVoiceNotes(query: String): List<VoiceNoteItemResult>


    // 특정 폴더 내 음성 메모 검색 (정상 상태, 조인 포함)
    @Query(
        """
        SELECT 
            vn.id as id,
            vn.title as title,
            vn.createdAt as createdAt,
            vn.updatedAt as updatedAt,
            vr.durationSec as duration,
            s.text as summary,
            f.name as folderName
        FROM voice_note vn
        LEFT JOIN voice_record vr ON vn.id = vr.voiceNoteId
        LEFT JOIN summary s ON vn.id = s.voiceNoteId
        LEFT JOIN folder f ON vn.folderId = f.id
        WHERE vn.folderId = :folderId -- 👈 해당 폴더 아이디 필터링
        AND vn.deletedAt IS NULL     -- 👈 삭제되지 않은 항목만
        AND vn.title LIKE '%' || :query || '%' -- 👈 검색어 포함
        ORDER BY vn.createdAt DESC
        """
    )
    suspend fun searchVoiceNoteInFolder(query: String, folderId: String): List<VoiceNoteItemResult>

    @Query("UPDATE voice_note SET folderId = :folderId, updatedAt = :updatedAt WHERE id IN (:voiceNoteId)")
    suspend fun moveToFolder(voiceNoteId: List<UUID>, folderId: String, updatedAt: Long)
}

