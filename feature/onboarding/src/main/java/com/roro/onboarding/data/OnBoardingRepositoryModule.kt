package com.roro.onboarding.data

import com.roro.onboarding.data.repository.OnBoardingRepositoryImpl
import com.roro.onboarding.domain.OnBoardingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class OnBoardingRepositoryModule {
    @Binds
    abstract fun bindOnBoardingRepository(
        impl: OnBoardingRepositoryImpl
    ): OnBoardingRepository
}