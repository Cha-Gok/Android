package com.roro.core.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.roro.core.entity.KeywordEntity
import com.roro.core.entity.VoiceNoteEntity
import java.util.UUID

/**
 * 기능 설명:
 * - KeywordDao 테이블에 대한 데이터 접근을 담당한다.
 * 아래는 임시 쿼리... 생성 후 각 쿼리 위에 기능 주석을 달아주세요...
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
@Dao
interface KeywordDao {

    /**
     * 키워드 저장
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(keyword: KeywordEntity)

    /**
     * 키워드 여러 개 저장
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(keywords: List<KeywordEntity>)

    // 키워드 가져오기
    @Query("SELECT * FROM keyword WHERE voiceNoteId = :voiceNoteId")
    suspend fun getByVoiceNoteId(voiceNoteId: UUID): List<KeywordEntity>

    /**
     * voiceNoteId에 해당하는 키워드 전체 삭제 (재생성 시 기존 키워드 제거용)
     */
    @Query("DELETE FROM keyword WHERE voiceNoteId = :voiceNoteId")
    suspend fun deleteByVoiceNoteId(voiceNoteId: UUID)

}