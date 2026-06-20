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
        private const val TAG = "RecordViewModel"
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
        Timber.tag(TAG).d("🔧 init - GemmaManager 초기화 시작")
        gemmaManager.initialize()
        Timber.tag(TAG).d("✅ init - GemmaManager 초기화 완료")
    }

    fun reset() {
        Timber.tag(TAG).d("🔄 reset() 호출")
        _state.value = RecordState.Idle
        _sttResult.value = ""
        _summarizeState.value = SummarizeState.Idle
        _amplitude.value = 0
        stopAmplitudePolling()
        Timber.tag(TAG).d("✅ reset() 완료")
    }

    private fun startAmplitudePolling() {
        Timber.tag(TAG).d("📊 startAmplitudePolling() 시작")
        amplitudeJob?.cancel()
        amplitudeJob = viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(AMPLITUDE_POLL_INTERVAL_MS)
                _amplitude.value = recordDataSource.getMaxAmplitude()
            }
        }
    }

    private fun stopAmplitudePolling() {
        Timber.tag(TAG).d("📊 stopAmplitudePolling() 호출")
        amplitudeJob?.cancel()
        amplitudeJob = null
        _amplitude.value = 0
    }

    @SuppressLint("MissingPermission")
    fun startRecording(folderName: String? = null) {
        Timber.tag(TAG).d("▶️ startRecording() 호출 - folderName=$folderName")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = recordDataSource.createAudioFile(folderName)
                Timber.tag(TAG).d("📁 오디오 파일 생성: ${file.absolutePath}")
                recordDataSource.startRecording(file)
                Timber.tag(TAG).d("🎙️ 녹음 시작됨 → state=Recording")
                _state.value = RecordState.Recording
                startAmplitudePolling()
                wakeLock.acquire(3 * 60 * 60 * 1000L)
                Timber.tag(TAG).d("🔒 WakeLock 획득")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "❌ startRecording 실패")
                _state.value = RecordState.Error(e.message ?: "녹음 시작 실패")
            }
        }
    }

    fun cancelRecording() {
        Timber.tag(TAG).d("🚫 cancelRecording() 호출")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                stopAmplitudePolling()
                recordDataSource.stopRecording()
                Timber.tag(TAG).d("🛑 녹음 취소됨")
                if (wakeLock.isHeld) {
                    wakeLock.release()
                    Timber.tag(TAG).d("🔓 WakeLock 해제")
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "❌ cancelRecording 실패")
            } finally {
                _state.value = RecordState.Idle
                Timber.tag(TAG).d("→ state=Idle")
            }
        }
    }

    fun stopRecording(folderId: UUID? = null) {
        Timber.tag(TAG).d("⏹️ stopRecording() 호출 - folderId=$folderId")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                stopAmplitudePolling()
                val file = recordDataSource.stopRecording()
                lastAudioFile = file
                Timber.tag(TAG).d("🎙️ 녹음 종료 - 파일: ${file.absolutePath}, 크기: ${file.length()}bytes")
                _state.value = RecordState.Processing
                Timber.tag(TAG).d("→ state=Processing")
                processAudio(file, folderId)
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "❌ stopRecording 실패")
                _state.value = RecordState.Error(e.message ?: "녹음 종료 실패")
            } finally {
                if (wakeLock.isHeld) {
                    wakeLock.release()
                    Timber.tag(TAG).d("🔓 WakeLock 해제")
                }
            }
        }
    }

    private suspend fun processAudio(file: File, folderId: UUID? = null) {
        Timber.tag(TAG).d("🔄 processAudio() 시작 - file=${file.name}, folderId=$folderId")
        try {
            // 1. STT
            Timber.tag(TAG).d("🗣️ STT 시작 - language=${_selectedLanguage.value}")
            val sttText = transcribeAudioUseCase(file, _selectedLanguage.value)
            Timber.tag(TAG).d("✅ STT 완료 - 결과 길이=${sttText.length}, 내용='${sttText.take(100)}'")

            // 2. 음성 없음
            if (sttText.isBlank()) {
                Timber.tag(TAG).d("⚠️ STT 결과 없음 → NoSpeech 처리")
                val voiceNoteId = saveRecordingUseCase(
                    audioFile = file,
                    durationSec = file.length() / (16000.0 * 2),
                    sttText = "",
                    summaryText = "",
                    keywords = emptyList(),
                    folderId = folderId,
                    summaryStatus = SummaryStatus.NONE
                )
                Timber.tag(TAG).d("💾 저장 완료 - voiceNoteId=$voiceNoteId → state=NoSpeech")
                _state.value = RecordState.NoSpeech(voiceNoteId.toString())
                return
            }

            // 3. 교정
            Timber.tag(TAG).d("✏️ 교정 시작")
            val proofreadText = try {
                val result = proofreadWithGemmaUseCase(sttText)
                Timber.tag(TAG).d("✅ 교정 완료 - 결과 길이=${result.length}")
                result
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "⚠️ 교정 실패 - 원본 STT 사용")
                sttText
            }
            _sttResult.value = proofreadText

            // 4. 키워드 + 요약
            Timber.tag(TAG).d("🔑 키워드 추출 시작")
            val keywords = try {
                val result = extractKeywordsUseCase(proofreadText)
                Timber.tag(TAG).d("✅ 키워드 추출 완료 - 개수=${result.size}, 키워드=$result")
                result
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "⚠️ 키워드 추출 실패")
                emptyList()
            }

            Timber.tag(TAG).d("📝 요약 시작")
            val summary = try {
                val result = summarizeTextUseCase(proofreadText)
                Timber.tag(TAG).d("✅ 요약 완료 - 결과 길이=${result.length}, 내용='${result.take(100)}'")
                result
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "⚠️ 요약 실패")
                ""
            }

            Timber.tag(TAG).d("💾 저장 시작 - summaryStatus=${if (summary.isBlank()) SummaryStatus.FAIL else SummaryStatus.SUCCESS}")
            val voiceNoteId = saveRecordingUseCase(
                audioFile = file,
                durationSec = file.length() / (16000.0 * 2),
                sttText = proofreadText,
                summaryText = summary,
                keywords = keywords,
                folderId = folderId,
                summaryStatus = if (summary.isBlank()) SummaryStatus.FAIL else SummaryStatus.SUCCESS
            )
            Timber.tag(TAG).d("✅ 저장 완료 - voiceNoteId=$voiceNoteId")

            // 5. 상태 분기
            val nextState = if (summary.isBlank()) {
                RecordState.SummaryError(voiceNoteId.toString())
            } else {
                RecordState.Success(voiceNoteId.toString())
            }
            Timber.tag(TAG).d("→ state=$nextState")
            _state.value = nextState

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "❌ processAudio 실패")
            _state.value = RecordState.Error(e.message ?: "처리 실패")
        }
    }

    fun retry(folderId: UUID? = null) {
        Timber.tag(TAG).d("🔁 retry() 호출 - folderId=$folderId, lastAudioFile=${lastAudioFile?.name}")
        val file = lastAudioFile ?: run {
            Timber.tag(TAG).w("⚠️ retry() - lastAudioFile 없음, 중단")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = RecordState.Processing
            Timber.tag(TAG).d("→ state=Processing, processAudio 재시작")
            processAudio(file, folderId)
        }
    }

    fun pauseRecording() {
        Timber.tag(TAG).d("⏸️ pauseRecording() 호출")
        stopAmplitudePolling()
        recordDataSource.pauseRecording()
        Timber.tag(TAG).d("✅ 녹음 일시정지됨")
    }

    fun resumeRecording() {
        Timber.tag(TAG).d("▶️ resumeRecording() 호출")
        recordDataSource.resumeRecording()
        startAmplitudePolling()
        Timber.tag(TAG).d("✅ 녹음 재개됨")
    }

    sealed class SummarizeState {
        object Idle : SummarizeState()
        object Loading : SummarizeState()
        data class Success(val summary: String) : SummarizeState()
        data class Error(val message: String) : SummarizeState()
    }
}