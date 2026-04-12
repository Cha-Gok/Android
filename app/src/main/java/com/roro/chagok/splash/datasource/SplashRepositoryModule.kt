package com.roro.chagok.splash.datasource

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SplashRepositoryModule {
    @Binds
    abstract fun bindSplashRepository(
        impl: SplashRepositoryImpl
    ): SplashRepository
}