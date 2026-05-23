package com.roro.core.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.roro.core.domain.model.FolderItemResult
import com.roro.core.entity.FolderEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * 기능 설명:
 * - Folder 테이블에 대한 데이터 접근을 담당한다.
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
@Dao
interface FolderDao {

    // --- 조회 (Read) ---

    // 단일 폴더 조회
    @Query("SELECT * FROM folder WHERE id = :id LIMIT 1")
    suspend fun getFolder(id: UUID): FolderEntity?

    // 사용자 폴더 목록 조회 (정상 상태)
    @Query(
        """
        SELECT * FROM folder
        WHERE deletedAt IS NULL
        ORDER BY updatedAt DESC
        """
    )
    fun observeUserFolders(): Flow<List<FolderEntity>>

    // 휴지통 폴더 조회 (삭제된 상태)
    @Query(
        """
        SELECT * FROM folder
        WHERE deletedAt IS NOT NULL
        ORDER BY updatedAt DESC
        """
    )
    fun observeTrashFolders(): Flow<List<FolderEntity>>


    // --- 생성 및 수정 (Create & Update) ---

    // 폴더 생성
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFolder(folder: FolderEntity)

    // 폴더 전체 업데이트 (객체 기반)
    @Update
    suspend fun updateFolder(folder: FolderEntity)

    // 폴더 이름 변경 (필드 기반)
    @Query(
        """
        UPDATE folder
        SET name = :name,
            updatedAt = :updatedAt
        WHERE id = :folderId
        """
    )
    suspend fun renameFolder(
        folderId: UUID,
        name: String,
        updatedAt: Long
    )


    // --- 휴지통 및 삭제 (Trash & Delete) ---

    // 휴지통으로 이동 (Soft Delete)
    @Query(
        """
        UPDATE folder
        SET deletedAt = :deletedAt, updatedAt = :updatedAt
        WHERE id = :folderId
        """
    )
    suspend fun moveToTrash(
        folderId: UUID,
        deletedAt: Long,
        updatedAt: Long
    )

    // 폴더 복원 (Restore)
    @Query(
        """
        UPDATE folder
        SET deletedAt = NULL, updatedAt = :updatedAt
        WHERE id = :folderId
        """
    )
    suspend fun restoreFolder(
        folderId: UUID,
        updatedAt: Long
    )

    // 폴더 영구 삭제 (Hard Delete)
    @Query("DELETE FROM folder WHERE id = :folderId")
    suspend fun removeFolder(folderId: UUID)


    // --- 검색 (Search) ---


    /**
     * 전체 폴더 검색 (정상 + 휴지통 상태 모두 포함)
     * - 각 폴더에 포함된 음성 메모(voice_note)의 개수를 합산하여 가져옵니다.
     */
    @Query(
        """
        SELECT 
            f.id as id,
            f.name as title, 
            f.createdAt as createAt,
            COUNT(vn.id) as count
        FROM folder f
        LEFT JOIN voice_note vn ON f.id = vn.folderId
        WHERE f.name LIKE '%' || :query || '%' -- 삭제 조건 제거
        GROUP BY f.id
        ORDER BY f.updatedAt DESC
    """
    )
    suspend fun searchFolders(query: String): List<FolderItemResult>

    // 폴더 검색 (휴지통)
    @Query(
        """
        SELECT 
            f.id as id,
            f.name as title, 
            f.createdAt as createAt,
            COUNT(vn.id) as count
        FROM folder f
        LEFT JOIN voice_note vn ON f.id = vn.folderId
        WHERE f.deletedAt IS NOT NULL 
        AND f.name LIKE '%' || :query || '%'
        GROUP BY f.id
        ORDER BY f.deletedAt DESC
    """
    )
    suspend fun searchTrashFolders(query: String): List<FolderItemResult>
}