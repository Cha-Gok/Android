package com.roro.storage.data

import com.roro.storage.data.repository.FileRepositoryImpl
import com.roro.storage.domain.FileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class FileRepositoryModule {
    @Binds
    abstract fun bindFileRepository(
        impl : FileRepositoryImpl
    ): FileRepository
}