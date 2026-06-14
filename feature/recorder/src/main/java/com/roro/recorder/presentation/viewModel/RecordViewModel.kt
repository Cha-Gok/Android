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

    // Gemma
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



    // 녹음 상태
    private val _state = MutableStateFlow<RecordState>(RecordState.Idle)
    val state: StateFlow<RecordState> = _state.asStateFlow()

    // STT 결과
    private val _sttResult = MutableStateFlow("")
    val sttResult: StateFlow<String> = _sttResult.asStateFlow()

    // 요약 결과
    private val _summarizeState = MutableStateFlow<SummarizeState>(SummarizeState.Idle)
    val summarizeState: StateFlow<SummarizeState> = _summarizeState.asStateFlow()

    // 언어 선택 - Locale → Language로 교체
    private val _selectedLanguage = MutableStateFlow(Language.KOREAN)
    val selectedLanguage: StateFlow<Language> = _selectedLanguage.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>() // voiceNoteId 전달
    val navigationEvent = _navigationEvent.asSharedFlow()

    // ✅ 추가
    private val _navigateToResult = MutableSharedFlow<Unit>()
    val navigateToResult = _navigateToResult.asSharedFlow()

    // ── Amplitude (실시간 음량) ──────────────────────────────
    private val _amplitude = MutableStateFlow(0)
    val amplitude: StateFlow<Int> = _amplitude.asStateFlow()

    private var amplitudeJob: Job? = null

    fun reset() {
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
                val amp = recordDataSource.getMaxAmplitude()
                //Timber.d("🎤 polling amp=$amp")  // 임시
                _amplitude.value = amp
            }
        }
    }

    private fun stopAmplitudePolling() {
        amplitudeJob?.cancel()
        amplitudeJob = null
        _amplitude.value = 0
    }
    // ────────────────────────────────────────────────────────


    init {
        gemmaManager.initialize()
    }

    /**
     * 녹음 시작
     */
    @SuppressLint("MissingPermission")
    fun startRecording(folderName: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = recordDataSource.createAudioFile(folderName)
                recordDataSource.startRecording(file)
                _state.value = RecordState.Recording
                startAmplitudePolling()
                wakeLock.acquire(3 * 60 * 60 * 1000L) // 최대 3시간
                Timber.tag(TAG).d("🎤 녹음 시작: ${file.absolutePath}")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "❌ 녹음 시작 실패")
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

    /**
     * 녹음 종료 → STT → 저장 → 요약
     */
    private var lastAudioFile: File? = null

    private val wakeLock by lazy {
        (context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager)
            .newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "chagok:RecordWakeLock")
    }


    fun stopRecording(folderId: UUID? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                stopAmplitudePolling()
                val file = recordDataSource.stopRecording()
                lastAudioFile = file
                _state.value = RecordState.Processing
                _navigateToResult.emit(Unit)
                processAudio(file, folderId)
            } catch (e: Exception) {
                _state.value = RecordState.Error(e.message ?: "녹음 종료 실패")
            } finally {
                if (wakeLock.isHeld) wakeLock.release()  // 처리 완료 후 해제
            }
        }
    }

    private suspend fun processAudio(file: File, folderId: UUID? = null) {
        try {
            val t0 = System.currentTimeMillis()

            // 1. STT
            val sttText = transcribeAudioUseCase(file, _selectedLanguage.value)
            Timber.tag(TAG).d("⏱️ STT: ${System.currentTimeMillis() - t0}ms")

            // 2. 음성 없음 → 파일만 저장
            if (sttText.isBlank()) {
                val voiceNoteId = saveRecordingUseCase(
                    audioFile = file,
                    durationSec = file.length() / (16000.0 * 2),
                    sttText = "",
                    summaryText = "",
                    keywords = emptyList(),
                    folderId = folderId
                )
                _state.value = RecordState.NoSpeech(voiceNoteId.toString())
                _navigationEvent.emit(voiceNoteId.toString())
                return
            }

            // 3. 교정
            val proofreadText = try {
                proofreadWithGemmaUseCase(sttText)
            } catch (e: Exception) {
                sttText // 교정 실패 시 원본 사용
            }
            _sttResult.value = proofreadText

            // 4. 키워드 + 요약
            val keywords = try {
                extractKeywordsUseCase(proofreadText)
            } catch (e: Exception) {
                emptyList()
            }

            val summary = try {
                summarizeTextUseCase(proofreadText)
            } catch (e: Exception) {
                ""
            }

            val durationSec = file.length() / (16000.0 * 2)
            val voiceNoteId = saveRecordingUseCase(
                audioFile = file,
                durationSec = durationSec,
                sttText = proofreadText,
                summaryText = summary,
                keywords = keywords,
                folderId = folderId
            )

            // 5. 요약 실패 여부에 따라 상태 분기
            if (summary.isBlank()) {
                _state.value = RecordState.SummaryError(voiceNoteId.toString())
            } else {
                _state.value = RecordState.Success(voiceNoteId.toString())
            }
            _navigationEvent.emit(voiceNoteId.toString())

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
        stopAmplitudePolling() // ← 추가
        recordDataSource.pauseRecording()
    }

    fun resumeRecording() {
        recordDataSource.resumeRecording()
        startAmplitudePolling() // ← 추가
    }


    // ── SummarizeState ─────────────────────────────────────
    sealed class SummarizeState {
        object Idle : SummarizeState()
        object Loading : SummarizeState()
        data class Success(val summary: String) : SummarizeState()
        data class Error(val message: String) : SummarizeState()
    }

    fun startTranscribeFromUri(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _sttResult.value = "인식 중..."

                val tmpFile = File(context.cacheDir, "test_audio.wav")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    tmpFile.writeBytes(input.readBytes())
                }

                val sttText = transcribeAudioUseCase(tmpFile, _selectedLanguage.value)
                _sttResult.value = sttText
                Timber.tag(TAG).d("🎤 STT 완료: $sttText")

                val keywords = extractKeywordsUseCase(sttText)
                Timber.tag(TAG).d("🔑 키워드 추출 완료: $keywords")

                _summarizeState.value = SummarizeState.Loading
                val summary = summarizeTextUseCase(sttText)
                _summarizeState.value = SummarizeState.Success(summary)
                Timber.tag(TAG).d("🤖 요약 완료: $summary")

                val durationSec = tmpFile.length() / (16000.0 * 2)
                val voiceNoteId = saveRecordingUseCase(
                    audioFile = tmpFile,
                    durationSec = durationSec,
                    sttText = sttText,
                    summaryText = summary,
                    keywords = keywords,
                    folderId = null
                )
                Timber.tag(TAG).d("💾 DB 저장 완료")
                _navigationEvent.emit(voiceNoteId.toString())

            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "🎤 URI STT 실패")
                _sttResult.value = "에러: ${e.message}"
                _summarizeState.value = SummarizeState.Error(e.message ?: "실패")
            }
        }
    }
}

