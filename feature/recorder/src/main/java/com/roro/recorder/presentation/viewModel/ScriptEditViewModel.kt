package com.roro.recorder.presentation.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roro.recorder.domain.usecase.GetVoiceNoteUseCase
import com.roro.recorder.domain.usecase.UpdateScriptUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

sealed class ScriptEditUiState {
    object Loading : ScriptEditUiState()
    object Idle : ScriptEditUiState()
    object Saving : ScriptEditUiState()
    object Saved : ScriptEditUiState()
    data class Error(val message: String) : ScriptEditUiState()
}

data class ScriptSegment(
    val startTimeMs: Long,
    val text: String
)

@HiltViewModel
class ScriptEditViewModel @Inject constructor(
    private val getVoiceNoteUseCase: GetVoiceNoteUseCase,  // ✅ DB 직접 조회
    private val updateScriptUseCase: UpdateScriptUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScriptEditUiState>(ScriptEditUiState.Loading)
    val uiState: StateFlow<ScriptEditUiState> = _uiState.asStateFlow()

    private val _segments = MutableStateFlow<List<ScriptSegment>>(emptyList())
    val segments: StateFlow<List<ScriptSegment>> = _segments.asStateFlow()

    private val _focusedIndex = MutableStateFlow<Int?>(null)
    val focusedIndex: StateFlow<Int?> = _focusedIndex.asStateFlow()

    private var originalText = ""

    // ✅ voiceNoteId로 DB에서 직접 sttText 조회
    fun load(voiceNoteId: String) {
        viewModelScope.launch {
            try {
                val result = getVoiceNoteUseCase(UUID.fromString(voiceNoteId))
                if (result != null) {
                    originalText = result.sttText
                    _segments.value = result.sttText
                        .split("\n")
                        .mapIndexed { index, text -> ScriptSegment(index * 30000L, text) }
                        .filter { it.text.isNotBlank() }
                    _uiState.value = ScriptEditUiState.Idle
                } else {
                    _uiState.value = ScriptEditUiState.Error("스크립트를 불러올 수 없어요")
                }
            } catch (e: Exception) {
                _uiState.value = ScriptEditUiState.Error(e.message ?: "오류가 발생했어요")
            }
        }
    }

    fun onSegmentChange(index: Int, newText: String) {
        _segments.value = _segments.value.toMutableList().also {
            it[index] = it[index].copy(text = newText)
        }
    }

    fun onSegmentFocused(index: Int) {
        _focusedIndex.value = index
    }

    fun onSegmentUnfocused() {
        _focusedIndex.value = null
    }

    private val currentText get() = _segments.value.joinToString("\n") { it.text }

    val isModified get() = currentText != originalText

    fun save(voiceNoteId: String) {
        if (!isModified) {
            _uiState.value = ScriptEditUiState.Saved
            return
        }
        viewModelScope.launch {
            _uiState.value = ScriptEditUiState.Saving
            try {
                updateScriptUseCase(
                    voiceNoteId = UUID.fromString(voiceNoteId),
                    newText = currentText
                )
                Timber.tag("ScriptEditVM").d("✅ 스크립트 저장 완료")
                _uiState.value = ScriptEditUiState.Saved
            } catch (e: Exception) {
                Timber.tag("ScriptEditVM").e(e, "❌ 스크립트 저장 실패")
                _uiState.value = ScriptEditUiState.Error(e.message ?: "저장 실패")
            }
        }
    }
}