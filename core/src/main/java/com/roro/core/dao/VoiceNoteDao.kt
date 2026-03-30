package com.roro.core.dao

import androidx.room.*
import com.roro.core.entity.VoiceNoteEntity
import com.roro.core.model.FolderWithNoteCount
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * 기능 설명:
 * - VoiceNoteDao 테이블에 대한 데이터 접근을 담당한다.
 * 아래는 임시 쿼리... 생성 후 각 쿼리 위에 기능 주석을 달아주세요...
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

    // 폴더를 가지고 있는 voiceNote 조회
    @Query(
        """
SELECT * FROM voice_note
WHERE folderId = :folderId
AND deletedAt IS NULL
ORDER BY createdAt DESC
"""
    )
    fun observeVoiceNote(folderId: UUID): Flow<List<VoiceNoteEntity>>

    // 폴더가 없는 voiceNote 조회
    @Query(
        """
SELECT * FROM voice_note
WHERE folderId IS NULL
AND deletedAt IS NULL
ORDER BY createdAt DESC
"""
    )
    fun observeFolderNullVoiceNote(): Flow<List<VoiceNoteEntity>>


    // 휴지통 VoiceNote 조회
    @Query(
        """
SELECT vn.* FROM voice_note vn
LEFT JOIN folder f ON vn.folderId = f.id
WHERE vn.deletedAt IS NOT NULL
AND (
    vn.folderId IS NULL
    OR f.deletedAt IS NULL
)
ORDER BY vn.deletedAt DESC
"""
    )
    fun observeTrashVoiceNotes(): Flow<List<VoiceNoteEntity>>

    // 최근 업데이트된 VoiceNote 상위 5개 조회
    @Query(
        """
SELECT * FROM voice_note
WHERE deletedAt IS NULL
ORDER BY updatedAt DESC
LIMIT 5
"""
    )
    fun observeRecentVoiceNote(): Flow<List<VoiceNoteEntity>>

    /**
     * 새 VoiceNote 생성
     * - 이미 동일한 ID가 존재하면 예외 발생 (정상적인 "생성" 동작)
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(note: VoiceNoteEntity)

    /**
     * VoiceNote 저장 또는 업데이트 (Upsert)
     * - 동일한 ID가 존재하면 기존 데이터를 교체
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: VoiceNoteEntity)

    @Delete
    suspend fun delete(note: VoiceNoteEntity)

    /**
     * 특정 폴더에 속한 VoiceNote 전체를 휴지통으로 이동
     */
    @Query(
        """
    UPDATE voice_note
    SET deletedAt = :deletedAt, updatedAt = :updatedAt
    WHERE folderId = :folderId AND deletedAt IS NULL
"""
    )
    suspend fun moveToTrashByFolderId(
        folderId: UUID,
        deletedAt: Long,
        updatedAt: Long
    )

    /*
* 여러개의 VoiceNOte 삭제
* */
    @Query(
        """
UPDATE voice_note
SET deletedAt = :deletedAt, updatedAt = :updatedAt
WHERE id IN (:noteIds)
"""
    )
    suspend fun moveNotesToTrash(
        noteIds: List<UUID>,
        deletedAt: Long,
        updatedAt: Long
    )


    /**
     * 특정 폴더에 속한 VoiceNote 전체 복원
     * - 폴더 복원 시 함께 사용
     * - 기존 folderId 유지
     */
    @Query(
        """
    UPDATE voice_note
    SET deletedAt = NULL, updatedAt = :updatedAt
    WHERE folderId = :folderId
"""
    )
    suspend fun restoreByFolderId(
        folderId: UUID,
        updatedAt: Long
    )

    /**
     * 개별 VoiceNote 복원
     * - 기존 folderId 유지
     */
    @Query(
        """
    UPDATE voice_note
    SET deletedAt = NULL, updatedAt = :updatedAt
    WHERE id = :noteId
"""
    )
    suspend fun restoreVoiceNote(
        noteId: UUID,
        updatedAt: Long
    )

    /**
     * 개별 VoiceNote를 전체 노트(null 폴더)로 복원
     * - 원래 폴더 없이 복원할 때 사용
     */
    @Query(
        """
    UPDATE voice_note
    SET folderId = NULL, deletedAt = NULL, updatedAt = :updatedAt
    WHERE id = :noteId
"""
    )
    suspend fun restoreVoiceNoteToRoot(
        noteId: UUID,
        updatedAt: Long
    )

    // voiceNote 제거
    @Delete
    suspend fun removeVoiceNote(voiceNoteEntity: VoiceNoteEntity)

    // 폴더가 있는 voiceNote 제거
    @Query(
        """
DELETE FROM voice_note
WHERE folderId = :folderId
"""
    )
    suspend fun removeVoiceNotesByFolderId(folderId: UUID)


    @Query(
        """
UPDATE voice_note
SET title = :voiceNoteTitle,
    updatedAt = :updatedAt
WHERE id = :noteId
"""
    )
    suspend fun renameVoiceNote(
        noteId: UUID,
        voiceNoteTitle: String,
        updatedAt: Long
    )
}