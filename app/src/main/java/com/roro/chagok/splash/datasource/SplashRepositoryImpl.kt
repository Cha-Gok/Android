package com.roro.chagok.splash.datasource

import com.roro.core.datastore.AppDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SplashRepositoryImpl @Inject constructor(
    private val dataStore: AppDataStore
) : SplashRepository {
    override fun getOnboardingStatus(): Flow<Boolean> {
        return dataStore.onboardingCompletedFlow
    }

}