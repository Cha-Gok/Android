package com.roro.core.gemma

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed class DownloadBottomSheetState {
    object Idle : DownloadBottomSheetState()
    data class Downloading(val progress: Float) : DownloadBottomSheetState()
    object Completed : DownloadBottomSheetState()
    object NetworkError : DownloadBottomSheetState()
    data class UnknownError(val message: String) : DownloadBottomSheetState()
}

@HiltViewModel
class GemmaDownloadBottomSheetViewModel @Inject constructor(
    private val gemmaDownloadManager: GemmaDownloadManager,
    private val gemmaManager: GemmaManager
) : ViewModel() {

    private val _state = MutableStateFlow<DownloadBottomSheetState>(DownloadBottomSheetState.Idle)
    val state: StateFlow<DownloadBottomSheetState> = _state.asStateFlow()

    fun startDownload() {

        Timber.tag("DownloadBS").d("🚀 startDownload 호출됨")

        viewModelScope.launch {
            gemmaDownloadManager.download().collect { downloadState ->
                Timber.tag("DownloadBS").d("📥 downloadState: $downloadState")
                _state.value = when (downloadState) {
                    is GemmaDownloadState.Downloading ->
                        DownloadBottomSheetState.Downloading(downloadState.progress)
                    is GemmaDownloadState.Completed -> {
                        gemmaManager.initialize()
                        DownloadBottomSheetState.Completed
                    }
                    is GemmaDownloadState.Error.NetworkLost ->
                        DownloadBottomSheetState.NetworkError
                    is GemmaDownloadState.Error.Unknown ->
                        DownloadBottomSheetState.UnknownError(downloadState.message)
                    else -> _state.value
                }
            }
        }
    }

    fun resetState() {
        Timber.tag("DownloadBS").d("🔄 resetState 호출됨")
        _state.value = DownloadBottomSheetState.Idle
    }
}