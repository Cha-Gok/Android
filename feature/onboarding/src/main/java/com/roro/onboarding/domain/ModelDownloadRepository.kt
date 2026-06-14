package com.roro.onboarding.domain

import com.roro.core.gemma.DeviceSupportResult
import com.roro.core.gemma.GemmaDownloadState
import kotlinx.coroutines.flow.Flow

interface ModelDownloadRepository {
    fun downloadGemma(): Flow<GemmaDownloadState>
    fun isModelDownloaded(): Boolean
    fun checkDeviceSupport(): DeviceSupportResult
}