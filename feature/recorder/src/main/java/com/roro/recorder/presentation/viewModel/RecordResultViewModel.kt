package com.roro.recorder.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roro.recorder.domain.usecase.GetVoiceNoteUseCase
import com.roro.recorder.domain.usecase.VoiceNoteResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed class RecordResultUiState {
    object Loading : RecordResultUiState()
    data class Success(val result: VoiceNoteResult) : RecordResultUiState()
    data class Error(val message: String) : RecordResultUiState()
}

@HiltViewModel
class RecordResultViewModel @Inject constructor(
    private val getVoiceNoteUseCase: GetVoiceNoteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecordResultUiState>(RecordResultUiState.Loading)
    val uiState: StateFlow<RecordResultUiState> = _uiState.asStateFlow()

    fun load(voiceNoteId: String) {
        viewModelScope.launch {
            try {
                val id = UUID.fromString(voiceNoteId)
                val result = getVoiceNoteUseCase(id)
                if (result != null) {
                    _uiState.value = RecordResultUiState.Success(result)
                } else {
                    _uiState.value = RecordResultUiState.Error("데이터를 찾을 수 없어요")
                }
            } catch (e: Exception) {
                _uiState.value = RecordResultUiState.Error(e.message ?: "오류가 발생했어요")
            }
        }
    }
}