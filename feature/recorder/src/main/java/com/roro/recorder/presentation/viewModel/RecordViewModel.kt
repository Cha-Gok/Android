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
import com.roro.recorder.data.datasource.RecordDataSource
import com.roro.recorder.domain.usecase.ExtractKeywordsUseCase
import com.roro.recorder.domain.usecase.SaveRecordingUseCase
import com.roro.recorder.domain.usecase.SummarizeTextSimpleUseCase
import com.roro.recorder.domain.usecase.SummarizeTextUseCase
import com.roro.recorder.domain.usecase.TranscribeAudioUseCase
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
    //private val summarizeTextUseCase: SummarizeTextUseCase, // 다시
    private val summarizeTextUseCase: SummarizeTextSimpleUseCase,
    private val extractKeywordsUseCase: ExtractKeywordsUseCase,
) : ViewModel() {

    companion object {
        private const val TAG = "RecordVM"
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

    // 언어 선택 (기본: 한국어)
    private val _selectedLocale = MutableStateFlow(Locale("ko", "KR"))
    val selectedLocale: StateFlow<Locale> = _selectedLocale.asStateFlow()


    private val _navigationEvent = MutableSharedFlow<String>() // voiceNoteId 전달
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun setLocale(locale: Locale) {
        _selectedLocale.value = locale
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
    private var lastAudioFile: File? = null  // 파일 보관용

    fun stopRecording(folderId: UUID? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = recordDataSource.stopRecording()
                lastAudioFile = file  // ← 파일 저장
                _state.value = RecordState.Processing
                processAudio(file, folderId)
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "❌ 녹음 종료 실패")
                _state.value = RecordState.Error(e.message ?: "녹음 종료 실패")
            }
        }
    }

    // STT ~ DB저장 로직을 별도 함수로 분리
    private suspend fun processAudio(file: File, folderId: UUID? = null) {
        try {
            val sttText = transcribeAudioUseCase(file, _selectedLocale.value)
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

    // 재시도 - 저장된 파일로 다시 처리
    fun retry(folderId: UUID? = null) {
        val file = lastAudioFile ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = RecordState.Processing
            processAudio(file, folderId)
        }
    }

    fun pauseRecording() {
        recordDataSource.pauseRecording()
    }

    fun resumeRecording() {
        recordDataSource.resumeRecording()
    }

    /**
     * STT 모델 상태 확인 + 다운로드
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

    // 파일 선택 -> stt 변환 확인 (필수 x. 테스트 확인용)
    fun startTranscribeFromUri(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _sttResult.value = "인식 중..."

                // Uri → 임시 파일로 복사
                val tmpFile = File(context.cacheDir, "test_audio.wav")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    tmpFile.writeBytes(input.readBytes())
                }

                // STT
                val sttText = transcribeAudioUseCase(tmpFile, _selectedLocale.value)
                _sttResult.value = sttText
                Timber.tag(TAG).d("🎤 STT 완료: $sttText")

                // 키워드 추출 추가
                val keywords = extractKeywordsUseCase(sttText)
                Timber.tag(TAG).d("🔑 키워드 추출 완료: $keywords")

                // 요약
                _summarizeState.value = SummarizeState.Loading
                val summary = summarizeTextUseCase(sttText)
                _summarizeState.value = SummarizeState.Success(summary)
                Timber.tag(TAG).d("🤖 요약 완료: $summary")

                // DB 저장
                val durationSec = tmpFile.length() / (16000.0 * 2)
                val voiceNoteId = saveRecordingUseCase(  // ✅ val로 받기
                    audioFile = tmpFile,
                    durationSec = durationSec,
                    sttText = sttText,
                    summaryText = summary,
                    keywords = keywords,
                    folderId = null
                )
                Timber.tag(TAG).d("💾 DB 저장 완료")
                _navigationEvent.emit(voiceNoteId.toString())  // ✅ 추가

            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "🎤 URI STT 실패")
                _sttResult.value = "에러: ${e.message}"
                _summarizeState.value = SummarizeState.Error(e.message ?: "실패")
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

}