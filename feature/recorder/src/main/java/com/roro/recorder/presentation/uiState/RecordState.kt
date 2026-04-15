package com.roro.recorder.presentation.uiState

// 재생 정시 재개 관련 상태 데이터 모음
sealed class RecordState {
    object Idle : RecordState()
    object Recording : RecordState()
    object Processing : RecordState()
    data class Success(val summary: String) : RecordState()
    data class Error(val message: String) : RecordState()
}