package com.roro.core.gemma

sealed class GemmaDownloadState {
    object Idle : GemmaDownloadState()
    data class Downloading(val progress: Float) : GemmaDownloadState()
    object Completed : GemmaDownloadState()
    sealed class Error : GemmaDownloadState() {
        object NetworkLost : Error()
        data class Unknown(val message: String) : Error()
    }
}