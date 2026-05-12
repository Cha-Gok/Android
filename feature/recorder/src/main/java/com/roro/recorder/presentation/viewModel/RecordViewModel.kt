package com.roro.recorder.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.summarization.Summarization
import com.google.mlkit.genai.summarization.SummarizationRequest
import com.google.mlkit.genai.summarization.SummarizerOptions
import com.google.mlkit.genai.summarization.Summarizer
//import com.roro.recorder.data.datasource.RecordDataSource
import com.roro.recorder.domain.repository.RecordRepository
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
import androidx.concurrent.futures.await
import com.google.mlkit.common.model.DownloadConditions


import com.google.mlkit.genai.common.DownloadCallback
import com.google.mlkit.genai.speechrecognition.speechRecognizerOptions

import com.google.mlkit.genai.speechrecognition.SpeechRecognition
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerOptions
import com.google.mlkit.genai.speechrecognition.speechRecognizerOptions
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.audio.AudioSource
import com.google.mlkit.genai.speechrecognition.SpeechRecognizer
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerRequest
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerResponse
import com.google.mlkit.genai.speechrecognition.speechRecognizerRequest
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.roro.core.datastore.Language
import com.roro.recorder.data.GemmaManager
import com.roro.recorder.data.datasource.RecordDataSource
import com.roro.recorder.domain.usecase.ExtractKeywordsUseCase
import com.roro.recorder.domain.usecase.SaveRecordingUseCase
import com.roro.recorder.domain.usecase.SummarizeTextSimpleUseCase
import com.roro.recorder.domain.usecase.SummarizeTextUseCase
import com.roro.recorder.domain.usecase.TranscribeAudioUseCase
import com.roro.recorder.domain.usecase.gemma.ExtractKeywordsWithGemmaUseCase
import com.roro.recorder.domain.usecase.gemma.SummarizeWithGemmaUseCase
import dagger.hilt.android.internal.Contexts.getApplication
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

import kotlinx.coroutines.tasks.await

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val recordDataSource: RecordDataSource,
    private val transcribeAudioUseCase: TranscribeAudioUseCase,
    private val saveRecordingUseCase: SaveRecordingUseCase,
    //private val summarizeTextUseCase: SummarizeTextSimpleUseCase,
    //private val extractKeywordsUseCase: ExtractKeywordsUseCase,

    // Gemma
    private val summarizeTextUseCase: SummarizeWithGemmaUseCase,
    private val extractKeywordsUseCase: ExtractKeywordsWithGemmaUseCase,
    private val gemmaManager: GemmaManager,

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

    fun setLanguage(language: Language) {
        _selectedLanguage.value = language
    }

    init {
        gemmaManager.initialize()

        // 테스트 확인용
//        viewModelScope.launch {
//            delay(10000L) // 초기화 기다리기
//            val result = gemmaManager.generate("안녕하세요! 간단히 자기소개 해주세요.")
//            Timber.tag("GemmaTest").d("🤖 응답: $result")
//        }
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
                startAmplitudePolling() // ← 추가
                Timber.tag(TAG).d("🎤 녹음 시작: ${file.absolutePath}")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "❌ 녹음 시작 실패")
                _state.value = RecordState.Error(e.message ?: "녹음 시작 실패")
            }
        }
    }

    /**
     * 녹음 종료 → STT → 저장 → 요약
     */
    private var lastAudioFile: File? = null


    fun stopRecording(folderId: UUID? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                stopAmplitudePolling()
                val file = recordDataSource.stopRecording()
                lastAudioFile = file
                _state.value = RecordState.Processing
                _navigateToResult.emit(Unit) // ✅ 즉시 화면 이동
                processAudio(file, folderId) // 백그라운드 처리
            } catch (e: Exception) {
                _state.value = RecordState.Error(e.message ?: "녹음 종료 실패")
            }
        }
    }
    private suspend fun processAudio(file: File, folderId: UUID? = null) {
        try {
            val sttText = transcribeAudioUseCase(file, _selectedLanguage.value)
            _sttResult.value = sttText

            val keywords = extractKeywordsUseCase(sttText)

            _summarizeState.value = SummarizeState.Loading
            val summary = summarizeTextUseCase(sttText)
            _summarizeState.value = SummarizeState.Success(summary)

            val durationSec = file.length() / (16000.0 * 2)
            val voiceNoteId = saveRecordingUseCase(
                audioFile = file,
                durationSec = durationSec,
                sttText = sttText,
                summaryText = summary,
                keywords = keywords,
                folderId = folderId
            )

            _state.value = RecordState.Success(sttText)
            _navigationEvent.emit(voiceNoteId.toString())

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "❌ 처리 실패")
            _state.value = RecordState.Error(e.message ?: "처리 실패")
            _summarizeState.value = SummarizeState.Error(e.message ?: "요약 실패")
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

    /**
     * STT 모델 상태 확인 + 다운로드 -> 온보딩
     */
    fun checkSTT() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val options = speechRecognizerOptions {
                    locale = Locale("ko", "KR")
                    preferredMode = SpeechRecognizerOptions.Mode.MODE_BASIC
                }
                val speechRecognizer = SpeechRecognition.getClient(options)
                val status = speechRecognizer.checkStatus()
                Timber.tag(TAG).d("🎤 STT 상태: $status")

                when (status) {
                    FeatureStatus.UNAVAILABLE -> Timber.tag(TAG).w("🎤 미지원 (UNAVAILABLE)")
                    FeatureStatus.DOWNLOADABLE -> {
                        speechRecognizer.download().collect { downloadStatus ->
                            when (downloadStatus) {
                                is DownloadStatus.DownloadStarted ->
                                    Timber.tag(TAG).d("🎤 다운로드 시작: ${downloadStatus.bytesToDownload / 1024 / 1024}MB")
                                is DownloadStatus.DownloadProgress ->
                                    Timber.tag(TAG).d("🎤 다운로드 중: ${downloadStatus.totalBytesDownloaded / 1024 / 1024}MB")
                                is DownloadStatus.DownloadCompleted ->
                                    Timber.tag(TAG).d("🎤 다운로드 완료!")
                                is DownloadStatus.DownloadFailed ->
                                    Timber.tag(TAG).e("🎤 다운로드 실패: $downloadStatus")
                            }
                        }
                    }
                    FeatureStatus.DOWNLOADING -> Timber.tag(TAG).d("🎤 다운로드 중...")
                    FeatureStatus.AVAILABLE -> Timber.tag(TAG).d("🎤 사용 가능!")
                }
                speechRecognizer.close()
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "🎤 STT 체크 실패")
            }
        }
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

    // ====================================================
    // TODO: 키워드 추출 - Prompt API S25 지원 후 구현 예정
    // ====================================================
//    fun extractKeywords(sttText: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val model = Generation.getClient()
//                val status = model.checkStatus()
//
//                when (status) {
//                    FeatureStatus.UNAVAILABLE -> Timber.w("🤖 이 기기는 미지원")
//                    FeatureStatus.DOWNLOADABLE -> {
//                        model.download().collect { Timber.d("🤖 다운로드: $it") }
//                    }
//                    FeatureStatus.AVAILABLE -> {
//                        model.warmup()
//                        val result = model.generateContent(
//                            "Extract 5 keywords from this text. " +
//                            "Return only the keywords separated by commas, no explanation.\n\n" +
//                            "Text: $sttText"
//                        )
//                        Timber.tag(TAG).d("🤖 키워드: ${result.candidates.first()}")
//                        // TODO: KeywordDao에 저장
//                    }
//                }
//                model.close()
//            } catch (e: Exception) {
//                Timber.e(e, "🤖 키워드 추출 실패")
//            }
//        }
//    }


    // ====================================================
    // TODO: 번역 - 필요 시 TranslateTextUseCase 연결
    // ====================================================
//    fun translateAndSummarize(context: Context, koreanText: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                _translateState.value = TranslateState.Loading
//                val englishText = translateTextUseCase(koreanText)  // 한국어 → 영어
//                _translateState.value = TranslateState.Success(englishText)
//                summarizeTextUseCase(englishText)
//            } catch (e: Exception) {
//                _translateState.value = TranslateState.Error(e.message ?: "번역 실패")
//            }
//        }
//    }
//
//    sealed class TranslateState {
//        object Idle : TranslateState()
//        object Loading : TranslateState()
//        data class Success(val text: String) : TranslateState()
//        data class Error(val message: String) : TranslateState()
//    }
//    private val _translateState = MutableStateFlow<TranslateState>(TranslateState.Idle)
//    val translateState = _translateState.asStateFlow()

