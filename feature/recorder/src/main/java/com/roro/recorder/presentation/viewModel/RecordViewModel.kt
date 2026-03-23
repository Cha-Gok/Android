package com.roro.recorder.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roro.recorder.domain.repository.RecordRepository
import com.roro.recorder.domain.repository.VoiceRecordRepository
import com.roro.recorder.domain.usecase.CreateVoiceNoteUseCase
import com.roro.recorder.domain.usecase.SaveVoiceRecordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

// UI 상태 관리

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val recordRepository: RecordRepository,           // 수정
    private val saveVoiceRecordUseCase: SaveVoiceRecordUseCase // CreateVoiceNoteUseCase 대신
) : ViewModel() {

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    fun startRecording() {
        recordRepository.startRecording()
        _isRecording.value = true
    }

    fun stopRecording(title: String) {
        val file = recordRepository.stopRecording()
        _isRecording.value = false

        viewModelScope.launch {
            val note = saveVoiceRecordUseCase(
                title = title,
                audioFilePath = file.absolutePath
            )
            Timber.d("✅ 저장 완료 - noteId: ${note.id}, title: ${note.title}, path: ${file.absolutePath}")
        }
    }
}