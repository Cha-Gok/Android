package com.roro.onboarding.data

import android.app.ActivityManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import android.os.Build
import com.roro.core.gemma.DeviceSupportResult
import com.roro.core.gemma.GemmaDownloadManager
import com.roro.core.gemma.GemmaDownloadState
import com.roro.core.gemma.GemmaManager
import javax.inject.Inject

class ModelDownloadDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gemmaDownloadManager: GemmaDownloadManager,
    private val gemmaManager: GemmaManager
) {
    fun downloadGemma(): Flow<GemmaDownloadState> {
        return gemmaDownloadManager.download()
            .onEach { state ->
                if (state is GemmaDownloadState.Completed) {
                    gemmaManager.initialize()
                }
            }
    }

    fun isModelDownloaded(): Boolean {
        return gemmaDownloadManager.isModelDownloaded()
    }

    fun checkDeviceSupport(): DeviceSupportResult {
        val isArm64 = Build.SUPPORTED_64_BIT_ABIS.any { it.contains("arm64") }

        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalRamGb = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
        val isEnoughRam = totalRamGb >= 4.0

        return when {
            !isArm64 -> DeviceSupportResult.UnsupportedCpu
            !isEnoughRam -> DeviceSupportResult.InsufficientRam(totalRamGb)
            else -> DeviceSupportResult.Supported
        }
    }
}