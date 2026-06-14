package com.roro.onboarding.data.repository

import com.roro.core.gemma.DeviceSupportResult
import com.roro.core.gemma.GemmaDownloadState
import com.roro.onboarding.data.ModelDownloadDataSource
import com.roro.onboarding.domain.ModelDownloadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ModelDownloadRepositoryImpl @Inject constructor(
    private val dataSource: ModelDownloadDataSource
) : ModelDownloadRepository {
    override fun downloadGemma(): Flow<GemmaDownloadState> =
        dataSource.downloadGemma()

    override fun isModelDownloaded(): Boolean =
        dataSource.isModelDownloaded()

    override fun checkDeviceSupport(): DeviceSupportResult =
        dataSource.checkDeviceSupport()
}