package com.roro.core.domain

import com.roro.core.datastore.Language
import kotlinx.coroutines.flow.Flow

interface CoreRepository {
    // 언어선택
    suspend fun setSelectedLanguage(language: Language)

    // 언어조회
    fun getSelectedLanguage(): Flow<Language>
}