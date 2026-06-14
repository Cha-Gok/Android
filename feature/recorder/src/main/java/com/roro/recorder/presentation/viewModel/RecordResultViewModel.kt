package com.roro.recorder.presentation.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.roro.recorder.domain.usecase.GetVoiceNoteUseCase
import com.roro.recorder.domain.usecase.RegenerateSummaryUseCase
import com.roro.recorder.domain.usecase.UpdateVoiceNoteTitleUseCase
import com.roro.recorder.domain.usecase.VoiceNoteResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.UUID
import javax.inject.Inject

sealed class RecordResultUiState {
    object Loading : RecordResultUiState()
    data class Success(val result: VoiceNoteResult) : RecordResultUiState()
    data class NoSpeech(val result: VoiceNoteResult) : RecordResultUiState()      // STT 없음
    data class SummaryError(val result: VoiceNoteResult) : RecordResultUiState()  // 요약 실패
    data class Error(val message: String) : RecordResultUiState()
}

enum class SummaryDisplayState {
    Success, Error, NoSpeech
}

data class PlayerUiState(
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isPlaying: Boolean = false
)

@HiltViewModel
class RecordResultViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getVoiceNoteUseCase: GetVoiceNoteUseCase,
    private val regenerateSummaryUseCase: RegenerateSummaryUseCase,
    private val updateVoiceNoteTitleUseCase: UpdateVoiceNoteTitleUseCase  // ✅
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecordResultUiState>(RecordResultUiState.Loading)
    val uiState: StateFlow<RecordResultUiState> = _uiState.asStateFlow()

    private val _playerUiState = MutableStateFlow(PlayerUiState())
    val playerUiState: StateFlow<PlayerUiState> = _playerUiState.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    private var _currentVoiceNoteId: UUID? = null  // 재생성 시 사용

    // ── VoiceNote 로드 ────────────────────────────────────────────────────────

    fun load(voiceNoteId: String) {
        viewModelScope.launch {
            try {
                val id = UUID.fromString(voiceNoteId)
                _currentVoiceNoteId = id
                val result = getVoiceNoteUseCase(id)
                if (result != null) {
                    _uiState.value = when {
                        result.sttText.isBlank() -> RecordResultUiState.NoSpeech(result)
                        result.summaryText.isBlank() -> RecordResultUiState.SummaryError(result)
                        else -> RecordResultUiState.Success(result)
                    }
                    preparePlayer(result.audioPath)
                } else {
                    _uiState.value = RecordResultUiState.Error("데이터를 찾을 수 없어요")
                }
            } catch (e: Exception) {
                _uiState.value = RecordResultUiState.Error(e.message ?: "오류가 발생했어요")
            }
        }
    }

    // ── ExoPlayer 초기화 (Main thread 필수) ──────────────────────────────────

    fun preparePlayer(audioPath: String) {
        if (audioPath.isBlank()) return

        viewModelScope.launch(Dispatchers.Main) {  // ✅ ExoPlayer는 Main thread에서만 생성/제어
            exoPlayer?.release()
            exoPlayer = ExoPlayer.Builder(context).build().also { player ->
                val mediaItem = MediaItem.fromUri(File(audioPath).toURI().toString())
                player.setMediaItem(mediaItem)
                player.prepare()
            }
            startPositionUpdater()
        }
    }

    // 재생 위치 100ms마다 업데이트, isActive로 코루틴 종료 시 자동 중단
    private fun startPositionUpdater() {
        viewModelScope.launch(Dispatchers.Main) {
            while (isActive) {
                val player = exoPlayer
                if (player != null) {
                    _playerUiState.value = PlayerUiState(
                        currentPositionMs = player.currentPosition,
                        durationMs = player.duration.coerceAtLeast(0L),
                        isPlaying = player.isPlaying
                    )
                }
                delay(100L)
            }
        }
    }

    // ── 플레이어 컨트롤 (모두 Main thread) ───────────────────────────────────

    fun play() {
        viewModelScope.launch(Dispatchers.Main) { exoPlayer?.play() }
    }

    fun pause() {
        viewModelScope.launch(Dispatchers.Main) { exoPlayer?.pause() }
    }

    fun seekTo(positionMs: Long) {
        viewModelScope.launch(Dispatchers.Main) { exoPlayer?.seekTo(positionMs) }
    }

    fun rewind5() {
        viewModelScope.launch(Dispatchers.Main) {
            val player = exoPlayer ?: return@launch
            player.seekTo((player.currentPosition - 5_000L).coerceAtLeast(0L))
        }
    }

    fun forward5() {
        viewModelScope.launch(Dispatchers.Main) {
            val player = exoPlayer ?: return@launch
            player.seekTo((player.currentPosition + 5_000L).coerceAtMost(player.duration))
        }
    }

    // ── 제목 편집 ─────────────────────────────────────────────────────────────

    private val _isTitleEditing = MutableStateFlow(false)
    val isTitleEditing: StateFlow<Boolean> = _isTitleEditing.asStateFlow()

    private val _editingTitle = MutableStateFlow("")
    val editingTitle: StateFlow<String> = _editingTitle.asStateFlow()

    // 제목 탭 → 편집 모드 진입
    fun startTitleEdit() {
        val current = _uiState.value as? RecordResultUiState.Success ?: return
        _editingTitle.value = current.result.title
        _isTitleEditing.value = true
    }

    // 편집 중 텍스트 변경
    fun onTitleChange(value: String) {
        _editingTitle.value = value
    }

    // 완료 버튼 or 키보드 내리기 → 저장 후 편집 모드 종료
    fun confirmTitleEdit() {
        val current = _uiState.value as? RecordResultUiState.Success ?: return
        val voiceNoteId = _currentVoiceNoteId ?: return
        _isTitleEditing.value = false

        viewModelScope.launch {
            val finalTitle = updateVoiceNoteTitleUseCase(
                voiceNoteId = voiceNoteId,
                newTitle = _editingTitle.value,
                currentTitle = current.result.title
            )
            // UiState 제목 갱신 + updatedAt 갱신
            _uiState.value = RecordResultUiState.Success(
                current.result.copy(
                    title = finalTitle,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // ── 재생성 ────────────────────────────────────────────────────────────────

    private val _isRegenerating = MutableStateFlow(false)
    val isRegenerating: StateFlow<Boolean> = _isRegenerating.asStateFlow()

    // 스크립트 수정 후 배너 + 빨간 점 표시용
    private val _isScriptModified = MutableStateFlow(false)
    val isScriptModified: StateFlow<Boolean> = _isScriptModified.asStateFlow()

    // ScriptEditScreen 저장 완료 후 호출
    fun onScriptSaved() {
        _isScriptModified.value = true
    }

    fun regenerateSummary() {
        val current = _uiState.value as? RecordResultUiState.Success ?: return

        viewModelScope.launch {
            _isRegenerating.value = true
            try {
                val regenerated = regenerateSummaryUseCase(
                    voiceNoteId = _currentVoiceNoteId ?: return@launch,
                    sttText = current.result.sttText
                )
                _uiState.value = RecordResultUiState.Success(
                    current.result.copy(
                        summaryText = regenerated.summaryText,
                        keywords = regenerated.keywords
                    )
                )
                _isScriptModified.value = false  // 재생성 완료 시 배너 + 점 사라짐
            } catch (e: Exception) {
                Timber.tag("RecordResultVM").e(e, "재생성 실패")
            } finally {
                _isRegenerating.value = false
            }
        }
    }

    // ── 정리 ─────────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch(Dispatchers.Main) {
            exoPlayer?.release()
            exoPlayer = null
        }
    }
}