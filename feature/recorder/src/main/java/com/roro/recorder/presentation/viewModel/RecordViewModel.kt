package com.roro.recorder.presentation

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roro.core.datastore.Language
import com.roro.core.domain.model.SummaryStatus
import com.roro.core.gemma.GemmaManager
import com.roro.recorder.data.datasource.RecordDataSource
import com.roro.recorder.domain.usecase.SaveRecordingUseCase
import com.roro.recorder.domain.usecase.UpdateSummaryAnalysisUseCase
import com.roro.recorder.domain.usecase.gemma.AnalyzeTranscriptWithGemmaUseCase
import com.roro.recorder.domain.usecase.gemma.ProofreadWithGemmaUseCase
import com.roro.recorder.domain.usecase.gemma.SttWithGemmaUseCase
import com.roro.recorder.domain.usecase.gemma.SummaryEligibility
import com.roro.recorder.presentation.uiState.RecordState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val recordDataSource: RecordDataSource,
    private val saveRecordingUseCase: SaveRecordingUseCase,
    private val gemmaManager: GemmaManager,
    private val proofreadWithGemmaUseCase: ProofreadWithGemmaUseCase,
    private val transcribeAudioUseCase: SttWithGemmaUseCase,
    private val analyzeTranscriptWithGemmaUseCase: AnalyzeTranscriptWithGemmaUseCase,
    private val updateSummaryAnalysisUseCase: UpdateSummaryAnalysisUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "RecordViewModel"
        private const val AMPLITUDE_POLL_INTERVAL_MS = 100L
        private const val PROOFREAD_MIN_TEXT_LENGTH = 300
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
        Timber.tag(TAG).d("init GemmaManager")
        gemmaManager.initialize()
    }

    fun reset() {
        Timber.tag(TAG).d("reset")
        _state.value = RecordState.Idle
        _sttResult.value = ""
        _summarizeState.value = SummarizeState.Idle
        _amplitude.value = 0
        stopAmplitudePolling()
    }

    private fun startAmplitudePolling() {
        Timber.tag(TAG).d("startAmplitudePolling")
        amplitudeJob?.cancel()
        amplitudeJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(AMPLITUDE_POLL_INTERVAL_MS)
                _amplitude.value = recordDataSource.getMaxAmplitude()
            }
        }
    }

    private fun stopAmplitudePolling() {
        Timber.tag(TAG).d("stopAmplitudePolling")
        amplitudeJob?.cancel()
        amplitudeJob = null
        _amplitude.value = 0
    }

    @SuppressLint("MissingPermission")
    fun startRecording(folderName: String? = null) {
        Timber.tag(TAG).d("startRecording folderName=$folderName")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = recordDataSource.createAudioFile(folderName)
                recordDataSource.startRecording(file)
                _state.value = RecordState.Recording
                startAmplitudePolling()
                wakeLock.acquire(3 * 60 * 60 * 1000L)
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "startRecording failed")
                _state.value = RecordState.Error(e.message ?: "Recording start failed")
            }
        }
    }

    fun cancelRecording() {
        Timber.tag(TAG).d("cancelRecording")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                stopAmplitudePolling()
                recordDataSource.stopRecording()
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "cancelRecording failed")
            } finally {
                _state.value = RecordState.Idle
            }
        }
    }

    fun stopRecording(folderId: UUID? = null) {
        Timber.tag(TAG).d("stopRecording folderId=$folderId")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                stopAmplitudePolling()
                val file = recordDataSource.stopRecording()
                lastAudioFile = file
                _state.value = RecordState.Processing
                processAudio(file, folderId)
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "stopRecording failed")
                _state.value = RecordState.Error(e.message ?: "Recording stop failed")
            } finally {
                if (wakeLock.isHeld) {
                    wakeLock.release()
                }
            }
        }
    }

    private suspend fun processAudio(file: File, folderId: UUID? = null) {
        Timber.tag(TAG).d("processAudio file=${file.name}, folderId=$folderId")
        try {
            val sttText = transcribeAudioUseCase(file, _selectedLanguage.value)

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
                _state.value = RecordState.NoSpeech(voiceNoteId.toString())
                return
            }

            val proofreadText = if (sttText.length < PROOFREAD_MIN_TEXT_LENGTH) {
                Timber.tag(TAG).d("skip proofread for short STT: length=${sttText.length}")
                sttText
            } else {
                try {
                    proofreadWithGemmaUseCase(sttText)
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "proofread failed; using original STT")
                    sttText
                }
            }
            _sttResult.value = proofreadText

            if (!SummaryEligibility.canSummarize(proofreadText)) {
                val voiceNoteId = saveRecordingUseCase(
                    audioFile = file,
                    durationSec = file.length() / (16000.0 * 2),
                    sttText = proofreadText,
                    summaryText = "",
                    keywords = emptyList(),
                    folderId = folderId,
                    summaryStatus = SummaryStatus.INSUFFICIENT
                )
                _state.value = RecordState.Success(voiceNoteId.toString())
                return
            }

            val voiceNoteId = saveRecordingUseCase(
                audioFile = file,
                durationSec = file.length() / (16000.0 * 2),
                sttText = proofreadText,
                summaryText = "",
                keywords = emptyList(),
                folderId = folderId,
                summaryStatus = SummaryStatus.GENERATING
            )
            _state.value = RecordState.Success(voiceNoteId.toString())
            startSummaryAnalysis(voiceNoteId, proofreadText)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "processAudio failed")
            _state.value = RecordState.Error(e.message ?: "Processing failed")
        }
    }

    private fun startSummaryAnalysis(voiceNoteId: UUID, proofreadText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Timber.tag(TAG).d("Summary analysis started")
                val analysis = analyzeTranscriptWithGemmaUseCase(proofreadText)
                updateSummaryAnalysisUseCase(voiceNoteId, analysis)
                Timber.tag(TAG).d("Summary analysis saved")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Summary analysis failed")
                updateSummaryAnalysisUseCase.markFailed(voiceNoteId)
            }
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
