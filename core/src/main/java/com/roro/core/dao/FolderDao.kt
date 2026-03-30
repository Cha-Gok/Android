package com.roro.core.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.roro.core.entity.FolderEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * 기능 설명:
 * - Folder 테이블에 대한 데이터 접근을 담당한다.
 * 아래는 임시 쿼리... 생성 후 각 쿼리 위에 기능 주석을 달아주세요...
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
@Dao
interface FolderDao {
    // 단일 폴더 조회
    @Query("SELECT * FROM folder WHERE id = :id LIMIT 1")
    suspend fun getFolder(id: UUID): FolderEntity?


    // 폴더 생성
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFolder(folder: FolderEntity)


    // 폴더 전체 업데이트
    @androidx.room.Update
    suspend fun updateFolder(folder: FolderEntity)

    // 휴지통으로 이동
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

    // 복원
    @Query("""
    UPDATE folder
    SET deletedAt = NULL, updatedAt = :updatedAt
    WHERE id = :folderId
""")
    suspend fun restoreFolder(
        folderId: UUID,
        updatedAt: Long
    )

    // 사용자 폴더 목록 조회(휴지통 제외)
    @Query(
        """
    SELECT * FROM folder
    WHERE deletedAt IS NULL
    ORDER BY updatedAt DESC
    """
    )
    fun observeUserFolders(): Flow<List<FolderEntity>>

    // 휴지통 폴더 조회
    @Query(
        """
        SELECT * FROM folder
        WHERE deletedAt IS NOT NULL
        ORDER BY updatedAt DESC
    """
    )
    fun observeTrashFolders(): Flow<List<FolderEntity>>

    // 폴더 이름 변경
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


    // 폴더 완전 삭제
    @Delete
    suspend fun removeFolder(folder: FolderEntity)

}