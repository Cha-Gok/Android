package com.roro.core.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.roro.core.entity.TranscriptEntity
import java.util.UUID

/**
 * 기능 설명:
 * - TranscriptDao 테이블에 대한 데이터 접근을 담당한다.
 * 아래는 임시 쿼리... 생성 후 각 쿼리 위에 기능 주석을 달아주세요...
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
@Dao
interface TranscriptDao {

    /**
     * 전사문 저장
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transcript: TranscriptEntity)

}