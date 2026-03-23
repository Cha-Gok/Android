package com.roro.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.roro.core.dao.FolderDao
import com.roro.core.dao.KeywordDao
import com.roro.core.dao.SummaryDao
import com.roro.core.dao.TranscriptDao
import com.roro.core.dao.VoiceNoteDao
import com.roro.core.dao.VoiceRecordDao
import com.roro.core.entity.FolderEntity
import com.roro.core.entity.KeywordEntity
import com.roro.core.entity.SummaryEntity
import com.roro.core.entity.TranscriptEntity
import com.roro.core.entity.VoiceNoteEntity
import com.roro.core.entity.VoiceRecordEntity

/**
 * 기능 설명:
 * - 앱에서 사용하는 Room 데이터베이스 정의
 * - Entity와 DAO를 연결하고 데이터 저장소 역할을 한다.
 * - 앱 전체에서 하나의 DB 인스턴스를 사용
 * - Hilt를 통해 Singleton으로 관리
 *
 * 포함된 테이블:
 * 1. FolderEntity      : 폴더 정보
 * 2. VoiceNoteEntity   : 녹음 메모 정보
 * 3. VoiceRecordEntity : 실제 녹음 파일 정보
 * 4. TranscriptEntity  : 음성 -> 텍스트 변환 결과
 * 5. SummaryEntity     : AI 요약 결과
 * 6. KeywordEntity     : 키워드 정보
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
@Database(
    entities = [
        FolderEntity::class,
        VoiceNoteEntity::class,
        VoiceRecordEntity::class,
        TranscriptEntity::class,
        SummaryEntity::class,
        KeywordEntity::class
    ],
    version = 9,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao
    abstract fun voiceNoteDao(): VoiceNoteDao
    abstract fun voiceRecordDao(): VoiceRecordDao
    abstract fun transcriptDao(): TranscriptDao
    abstract fun summaryDao(): SummaryDao
    abstract fun keywordDao(): KeywordDao
}