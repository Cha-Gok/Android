package com.roro.onboarding.domain

import javax.inject.Inject

class DownloadModelsUseCase @Inject constructor(
    private val repository: ModelDownloadRepository
) {
    suspend fun downloadSTT() = repository.downloadSTT()
    suspend fun downloadSummarize() = repository.downloadSummarize()
    suspend fun downloadTranslate() = repository.downloadTranslate()
}