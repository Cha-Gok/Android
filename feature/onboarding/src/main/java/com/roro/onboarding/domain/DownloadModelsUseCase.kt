package com.roro.onboarding.domain

import com.roro.core.gemma.DeviceSupportResult
import com.roro.core.gemma.GemmaDownloadState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DownloadModelsUseCase @Inject constructor(
    private val repository: ModelDownloadRepository
) {
    fun downloadGemma(): Flow<GemmaDownloadState> =
        repository.downloadGemma()

    fun isModelDownloaded(): Boolean =
        repository.isModelDownloaded()

    fun checkDeviceSupport(): DeviceSupportResult =
        repository.checkDeviceSupport()
}