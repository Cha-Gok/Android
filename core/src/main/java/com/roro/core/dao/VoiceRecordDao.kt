package com.roro.core.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.roro.core.entity.VoiceRecordEntity
import java.util.UUID

/**
 * 기능 설명:
 * - VoiceRecordDao 테이블에 대한 데이터 접근을 담당한다.
 * 아래는 임시 쿼리... 생성 후 각 쿼리 위에 기능 주석을 달아주세요...
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
@Dao
interface VoiceRecordDao {

    /**
     * 녹음 파일 정보 저장
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(record: VoiceRecordEntity)

    /**
     * 녹음 파일 정보 저장 또는 업데이트
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: VoiceRecordEntity)

    /**
     * voiceNoteId로 녹음 파일 정보 단건 조회
     */
    @Query("SELECT * FROM voice_record WHERE voiceNoteId = :voiceNoteId LIMIT 1")
    suspend fun getByVoiceNoteId(voiceNoteId: UUID): VoiceRecordEntity?
}