package com.roro.onboarding.data.repository

import com.roro.onboarding.data.ModelDownloadDataSource
import com.roro.onboarding.domain.ModelDownloadRepository
import javax.inject.Inject

class ModelDownloadRepositoryImpl @Inject constructor(
    private val dataSource: ModelDownloadDataSource
) : ModelDownloadRepository {
    override suspend fun downloadSTT() = dataSource.downloadSTT()
    override suspend fun downloadSummarize() = dataSource.downloadSummarize()
    override suspend fun downloadTranslate() = dataSource.downloadTranslate()
}