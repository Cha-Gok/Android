package com.roro.core.dao

import androidx.room.*
import com.roro.core.entity.VoiceNoteEntity
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: VoiceNoteEntity)

    @Delete
    suspend fun delete(note: VoiceNoteEntity)
}