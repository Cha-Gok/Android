package com.roro.recorder.presentation.uiState

// 재생 정시 재개 관련 상태 데이터 모음
sealed class RecordState {
    object Idle : RecordState()
    object Recording : RecordState()
    object Processing : RecordState()
    data class Success(val voiceNoteId: String) : RecordState()
    data class NoSpeech(val voiceNoteId: String) : RecordState()      // STT 결과 없음
    data class SummaryError(val voiceNoteId: String) : RecordState()  // 요약 실패
    data class Error(val message: String) : RecordState()             // 녹음/저장 자체 실패
}