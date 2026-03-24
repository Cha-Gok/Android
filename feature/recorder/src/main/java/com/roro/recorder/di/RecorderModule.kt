package com.roro.recorder.di

import android.content.Context
import com.roro.core.dao.VoiceNoteDao
import com.roro.core.dao.VoiceRecordDao
import com.roro.recorder.data.repository.RecordRepositoryImpl
import com.roro.recorder.data.repository.VoiceNoteRepositoryImpl
import com.roro.recorder.domain.repository.RecordRepository
import com.roro.recorder.domain.repository.VoiceNoteRepository

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class RecorderModule {

    @Binds
    abstract fun bindRecordRepository(
        impl: RecordRepositoryImpl
    ): RecordRepository

    @Binds
    abstract fun bindVoiceNoteRepository(
        impl: VoiceNoteRepositoryImpl
    ): VoiceNoteRepository
}