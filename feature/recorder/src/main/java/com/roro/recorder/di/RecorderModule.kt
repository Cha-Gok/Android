package com.roro.recorder.di

import android.content.Context
import com.roro.core.dao.VoiceNoteDao
import com.roro.core.dao.VoiceRecordDao
import com.roro.recorder.data.repository.RecordRepositoryImpl
import com.roro.recorder.data.repository.VoiceRecordRepositoryImpl
import com.roro.recorder.data.repository.VoiceNoteRepositoryImpl
import com.roro.recorder.domain.repository.RecordRepository
import com.roro.recorder.domain.repository.VoiceRecordRepository
import com.roro.recorder.domain.repository.VoiceNoteRepository
import com.roro.recorder.domain.usecase.CreateVoiceNoteUseCase
import com.roro.recorder.domain.usecase.SaveVoiceRecordUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RecorderModule {

    @Provides
    @Singleton
    fun provideRecordRepository(
        @ApplicationContext context: Context
    ): RecordRepository = RecordRepositoryImpl(context)  // 녹음만

    @Provides
    @Singleton
    fun provideVoiceNoteRepository(
        dao: VoiceNoteDao
    ): VoiceNoteRepository = VoiceNoteRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideVoiceRecordRepository(
        dao: VoiceRecordDao
    ): VoiceRecordRepository = VoiceRecordRepositoryImpl(dao)  // DB만

    @Provides
    @Singleton
    fun provideCreateVoiceNoteUseCase(
        repository: VoiceNoteRepository
    ): CreateVoiceNoteUseCase = CreateVoiceNoteUseCase(repository)

    @Provides
    @Singleton
    fun provideSaveVoiceRecordUseCase(
        voiceNoteRepository: VoiceNoteRepository,
        voiceRecordRepository: VoiceRecordRepository
    ): SaveVoiceRecordUseCase = SaveVoiceRecordUseCase(voiceNoteRepository, voiceRecordRepository)
}