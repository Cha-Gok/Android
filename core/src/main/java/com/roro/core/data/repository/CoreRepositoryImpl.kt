package com.roro.core.data.repository

import com.roro.core.data.datasource.DataStoreSource
import com.roro.core.datastore.Language
import com.roro.core.domain.CoreRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CoreRepositoryImpl @Inject constructor(
    private val dataStore: DataStoreSource
) : CoreRepository {
    override suspend fun setSelectedLanguage(language: Language) {
        dataStore.setLanguage(language)
    }

    override fun getSelectedLanguage(): Flow<Language> {
        return dataStore.getLanguage()
    }
}