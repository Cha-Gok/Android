package com.roro.onboarding.domain

interface ModelDownloadRepository {
    suspend fun downloadSTT()
    suspend fun downloadSummarize()
    suspend fun downloadTranslate()
}