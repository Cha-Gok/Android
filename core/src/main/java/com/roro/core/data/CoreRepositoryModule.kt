package com.roro.core.data

import com.roro.core.data.repository.CoreRepositoryImpl
import com.roro.core.domain.CoreRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreRepositoryModule {
    @Binds
    abstract fun bindCoreRepository(
        impl: CoreRepositoryImpl
    ): CoreRepository
}