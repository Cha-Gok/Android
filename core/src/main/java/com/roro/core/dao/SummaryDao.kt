package com.roro.core.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.roro.core.entity.KeywordEntity
import com.roro.core.entity.SummaryEntity
import java.util.UUID

/**
 * 기능 설명:
 * - SummaryDao 테이블에 대한 데이터 접근을 담당한다.
 * 아래는 임시 쿼리... 생성 후 각 쿼리 위에 기능 주석을 달아주세요...
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
@Dao
interface SummaryDao {

    /**
     * 요약문 저장
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(summary: SummaryEntity)

    //0414 추가
    /**
     * 요약문 불러오기
     */
    @Query("SELECT * FROM summary WHERE voiceNoteId = :voiceNoteId LIMIT 1")
    suspend fun getByVoiceNoteId(voiceNoteId: UUID): SummaryEntity?

    /**
     * 요약문 텍스트 업데이트 (재생성 시 사용)
     */
    @Query("UPDATE summary SET text = :text WHERE voiceNoteId = :voiceNoteId")
    suspend fun updateText(voiceNoteId: UUID, text: String)

}