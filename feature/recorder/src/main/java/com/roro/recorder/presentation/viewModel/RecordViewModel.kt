package com.roro.recorder.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roro.recorder.presentation.uiState.RecordState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import com.roro.core.datastore.Language
import com.roro.core.domain.model.SummaryStatus
import com.roro.core.gemma.GemmaManager
import com.roro.recorder.data.datasource.RecordDataSource
import com.roro.recorder.domain.usecase.SaveRecordingUseCase
import com.roro.recorder.domain.usecase.gemma.ExtractKeywordsWithGemmaUseCase
import com.roro.recorder.domain.usecase.gemma.ProofreadWithGemmaUseCase
import com.roro.recorder.domain.usecase.gemma.SttWithGemmaUseCase
import com.roro.recorder.domain.usecase.gemma.SummarizeWithGemmaUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale


@HiltViewModel
class RecordViewModel @Inject constructor(
    private val recordDataSource: RecordDataSource,
    private val saveRecordingUseCase: SaveRecordingUseCase,
    private val summarizeTextUseCase: SummarizeWithGemmaUseCase,
    private val extractKeywordsUseCase: ExtractKeywordsWithGemmaUseCase,
    private val gemmaManager: GemmaManager,
    private val proofreadWithGemmaUseCase: ProofreadWithGemmaUseCase,
    private val transcribeAudioUseCase: SttWithGemmaUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "RecordVM"
        private const val AMPLITUDE_POLL_INTERVAL_MS = 100L
    }

    private val _state = MutableStateFlow<RecordState>(RecordState.Idle)
    val state: StateFlow<RecordState> = _state.asStateFlow()

    private val _sttResult = MutableStateFlow("")
    val sttResult: StateFlow<String> = _sttResult.asStateFlow()

    private val _summarizeState = MutableStateFlow<SummarizeState>(SummarizeState.Idle)
    val summarizeState: StateFlow<SummarizeState> = _summarizeState.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(Language.KOREAN)
    val selectedLanguage: StateFlow<Language> = _selectedLanguage.asStateFlow()

    private val _amplitude = MutableStateFlow(0)
    val amplitude: StateFlow<Int> = _amplitude.asStateFlow()

    private var amplitudeJob: Job? = null
    private var lastAudioFile: File? = null

    private val wakeLock by lazy {
        (context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager)
            .newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "chagok:RecordWakeLock")
    }

    init {
        gemmaManager.initialize()
    }

    fun reset() {
        Timber.tag("문제").d("🔄 reset() 호출")
        _state.value = RecordState.Idle
        _sttResult.value = ""
        _summarizeState.value = SummarizeState.Idle
        _amplitude.value = 0
        stopAmplitudePolling()
    }



    private fun startAmplitudePolling() {
        amplitudeJob?.cancel()
        amplitudeJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(AMPLITUDE_POLL_INTERVAL_MS)
                _amplitude.value = recordDataSource.getMaxAmplitude()
            }
        }
    }

    private fun stopAmplitudePolling() {
        amplitudeJob?.cancel()
        amplitudeJob = null
        _amplitude.value = 0
    }

    @SuppressLint("MissingPermission")
    fun startRecording(folderName: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = recordDataSource.createAudioFile(folderName)
                recordDataSource.startRecording(file)
                _state.value = RecordState.Recording
                startAmplitudePolling()
                wakeLock.acquire(3 * 60 * 60 * 1000L)
            } catch (e: Exception) {
                _state.value = RecordState.Error(e.message ?: "녹음 시작 실패")
            }
        }
    }

    fun cancelRecording() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                stopAmplitudePolling()
                recordDataSource.stopRecording()
                if (wakeLock.isHeld) wakeLock.release()
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "cancel 실패")
            } finally {
                _state.value = RecordState.Idle
            }
        }
    }

    fun stopRecording(folderId: UUID? = null) {
        Timber.tag("문제").d("🎬 ViewModel.stopRecording() 호출")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                stopAmplitudePolling()
                val file = recordDataSource.stopRecording()
                lastAudioFile = file
                _state.value = RecordState.Processing
                processAudio(file, folderId)
            } catch (e: Exception) {
                Timber.tag("문제").d("❌ stopRecording 예외: ${e.message}")
                _state.value = RecordState.Error(e.message ?: "녹음 종료 실패")
            } finally {
                if (wakeLock.isHeld) wakeLock.release()
            }
        }
    }

    private suspend fun processAudio(file: File, folderId: UUID? = null) {
        try {
            // 1. STT
            val sttText = transcribeAudioUseCase(file, _selectedLanguage.value)

            // 2. 음성 없음
            if (sttText.isBlank()) {
                val voiceNoteId = saveRecordingUseCase(
                    audioFile = file,
                    durationSec = file.length() / (16000.0 * 2),
                    sttText = "",
                    summaryText = "",
                    keywords = emptyList(),
                    folderId = folderId,
                    summaryStatus = SummaryStatus.NONE
                )
                _state.value = RecordState.NoSpeech(voiceNoteId.toString())  // ✅ emit 없음
                return
            }

            // 3. 교정
            val proofreadText = try {
                proofreadWithGemmaUseCase(sttText)
            } catch (e: Exception) {
                sttText
            }
            _sttResult.value = proofreadText

            // 4. 키워드 + 요약
            val keywords = try { extractKeywordsUseCase(proofreadText) } catch (e: Exception) { emptyList() }
            val summary = try { summarizeTextUseCase(proofreadText) } catch (e: Exception) { "" }

            val voiceNoteId = saveRecordingUseCase(
                audioFile = file,
                durationSec = file.length() / (16000.0 * 2),
                sttText = proofreadText,
                summaryText = summary,
                keywords = keywords,
                folderId = folderId,
                summaryStatus = if (summary.isBlank()) SummaryStatus.FAIL else SummaryStatus.SUCCESS
            )

            // 5. 상태 분기  ✅ emit 없음
            _state.value = if (summary.isBlank()) {
                RecordState.SummaryError(voiceNoteId.toString())
            } else {
                RecordState.Success(voiceNoteId.toString())
            }

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "❌ 처리 실패")
            _state.value = RecordState.Error(e.message ?: "처리 실패")
        }
    }

    fun retry(folderId: UUID? = null) {
        val file = lastAudioFile ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = RecordState.Processing
            processAudio(file, folderId)
        }
    }

    fun pauseRecording() {
        stopAmplitudePolling()
        recordDataSource.pauseRecording()
    }

    fun resumeRecording() {
        recordDataSource.resumeRecording()
        startAmplitudePolling()
    }

    sealed class SummarizeState {
        object Idle : SummarizeState()
        object Loading : SummarizeState()
        data class Success(val summary: String) : SummarizeState()
        data class Error(val message: String) : SummarizeState()
    }
}
