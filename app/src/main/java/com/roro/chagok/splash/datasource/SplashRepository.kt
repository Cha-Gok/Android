package com.roro.chagok.splash.datasource

import kotlinx.coroutines.flow.Flow

interface SplashRepository {
    fun getOnboardingStatus(): Flow<Boolean>
}