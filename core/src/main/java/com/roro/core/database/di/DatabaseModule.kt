package com.roro.core.database.di

import android.content.Context
import androidx.room.Room
import com.roro.core.dao.FolderDao
import com.roro.core.dao.KeywordDao
import com.roro.core.dao.SummaryDao
import com.roro.core.dao.TranscriptDao
import com.roro.core.dao.VoiceNoteDao
import com.roro.core.dao.VoiceRecordDao
import com.roro.core.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 기능 설명:
 * -Room 데이터베이스(AppDatabase)를 Hilt DI에 제공하는 모듈
 * - 앱 전역에서 동일한 DB 인스턴스를 사용하도록 Singleton으로 관리
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDb(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "chagok.db")
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides
    fun provideFolderDao(db: AppDatabase): FolderDao = db.folderDao()

    @Provides
    fun provideKeywordDao(db: AppDatabase): KeywordDao = db.keywordDao()

    @Provides
    fun provideSummaryDao(db: AppDatabase): SummaryDao = db.summaryDao()

    @Provides
    fun provideTranscriptDao(db: AppDatabase): TranscriptDao = db.transcriptDao()

    @Provides
    fun provideVoiceNoteDao(db: AppDatabase): VoiceNoteDao = db.voiceNoteDao()

    @Provides
    fun provideVoiceRecordDao(db: AppDatabase): VoiceRecordDao = db.voiceRecordDao()

}