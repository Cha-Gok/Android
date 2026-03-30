package com.roro.recorder.presentation

import android.content.Context
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
import com.roro.storage.data.datasource.RecorderLocalFileDataSource
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


import com.google.mlkit.genai.common.DownloadCallback
import com.google.mlkit.genai.speechrecognition.speechRecognizerOptions

import com.google.mlkit.genai.speechrecognition.SpeechRecognition
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerOptions
import com.google.mlkit.genai.speechrecognition.speechRecognizerOptions
import com.google.mlkit.genai.common.DownloadStatus
import java.util.Locale

import kotlinx.coroutines.tasks.await

@HiltViewModel
class RecordViewModel @Inject constructor(
    //private val recordDataSource: RecordDataSource,
    private val fileDataSource: RecorderLocalFileDataSource,
    private val repository: RecordRepository
) : ViewModel() {

    companion object {
        private const val TAG = "RecordVM"
    }

    private val _state = MutableStateFlow<RecordState>(RecordState.Idle)
    val state: StateFlow<RecordState> = _state

    private var startTime: Long = 0L
    private var currentFile: File? = null

    /**
     * 녹음 시작
     */
    fun startRecording(folderName: String? = null) {
        Timber.tag(TAG).d("🎤 startRecording 호출됨")

        startTime = System.currentTimeMillis()

        try {
            val file = fileDataSource.createAudioFile(folderName)
            Timber.tag(TAG).d("📁 파일 생성됨: ${file.absolutePath}")

            currentFile = file

            //recordDataSource.startRecording(file)
            Timber.tag(TAG).d("✅ 녹음 시작 성공")

            _state.value = RecordState.Recording

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "❌ 녹음 시작 실패")
            _state.value = RecordState.Error(e.message ?: "녹음 시작 실패")
        }
    }

    /**
     * 녹음 종료 + 처리
     */
    fun stopRecording(folderId: UUID? = null) {
//        Timber.tag(TAG).d("🛑 stopRecording 호출됨")
//
//        val duration = System.currentTimeMillis() - startTime
//
//        if (duration < 1000) {
//            Timber.tag(TAG).e("❌ 녹음 너무 짧음: ${duration}ms")
//            return
//        }
//
//
//
//        viewModelScope.launch {
//            try {
//                _state.value = RecordState.Processing
//                Timber.tag(TAG).d("⏳ Processing 상태 진입")
//
//                // 1. 녹음 종료
//                recordDataSource.stopRecording()
//                Timber.tag(TAG).d("🛑 녹음 종료 완료")
//
//                val file = currentFile
//                    ?: throw IllegalStateException("녹음 파일 없음")
//
//                Timber.tag(TAG).d("📁 사용할 파일: ${file.absolutePath}")
//                Timber.tag(TAG).d("📏 파일 크기: ${file.length()} bytes")
//
//                // 2. 처리 + 저장
//                Timber.tag(TAG).d("🚀 STT + 요약 + 저장 시작")
//
//                val summary = repository.processAndSave(
//                    audioFile = file,
//                    folderId = folderId
//                )
//
//                Timber.tag(TAG).d("🎉 처리 완료! summary: $summary")
//
//                _state.value = RecordState.Success(summary)
//
//            } catch (e: Exception) {
//                Timber.tag(TAG).e(e, "❌ 처리 실패")
//                _state.value = RecordState.Error(e.message ?: "처리 실패")
//            }
//        }
    }

    fun checkSTT() {
        viewModelScope.launch {
            try {
                val options = speechRecognizerOptions {
                    locale = Locale.KOREAN  // 한국어 가능!
                    preferredMode = SpeechRecognizerOptions.Mode.MODE_BASIC // S25는 BASIC만
                }
                val speechRecognizer = SpeechRecognition.getClient(options)

                val status = speechRecognizer.checkStatus()
                Timber.d("🎤 STT 상태: $status")

                when (status) {
                    FeatureStatus.DOWNLOADABLE -> {
                        speechRecognizer.download().collect { downloadStatus ->
                            when (downloadStatus) {
                                is DownloadStatus.DownloadCompleted -> Timber.d("🎤 다운로드 완료!")
                                is DownloadStatus.DownloadFailed -> Timber.e("🎤 다운로드 실패")
                                is DownloadStatus.DownloadProgress -> Timber.d("🎤 다운로드 중...")
                                else -> {}
                            }
                        }
                    }
                    FeatureStatus.AVAILABLE -> {
                        Timber.d("🎤 STT 사용 가능!")
                    }
                    FeatureStatus.UNAVAILABLE -> {
                        Timber.w("🎤 이 기기 미지원")
                    }
                }

                speechRecognizer.close()

            } catch (e: Exception) {
                Timber.e(e, "🎤 STT 체크 실패")
            }
        }
    }

    // 요약 ai 연결 확인 여부
    fun checkAICore(context: Context) { viewModelScope.launch(Dispatchers.IO){
        try { val options = SummarizerOptions.builder(context)
            .setInputType(SummarizerOptions.InputType.ARTICLE)
            .setOutputType(SummarizerOptions.OutputType.ONE_BULLET)
            //.setLanguage(SummarizerOptions.Language.KOREAN)
            .build()

            val summarizer = Summarization.getClient(options)
            //val summarizer = Summarization.getClient(options)

            // ✅ Task를 코루틴으로 변환 (블로킹 .get() 대신)
            val status = summarizer.checkFeatureStatus().await()
            // 0=UNAVAILABLE, 1=DOWNLOADABLE, 2=DOWNLOADING, 3=AVAILABLE

            Timber.d("🤖 Summarization 상태: $status")

            when (status) {
                0 -> Timber.w("🤖 이 기기는 Summarization 미지원 (UNAVAILABLE)")
                1 -> {
                    Timber.d("🤖 모델 다운로드 필요 (DOWNLOADABLE)")
                    summarizer.downloadFeature(object : DownloadCallback {
                        override fun onDownloadStarted(bytesToDownload: Long) {
                            Timber.d("🤖 다운로드 시작: ${bytesToDownload}bytes")
                        }
                        override fun onDownloadProgress(bytesDownloaded: Long) {
                            Timber.d("🤖 다운로드 중: ${bytesDownloaded}bytes")
                        }
                        override fun onDownloadCompleted() {
                            Timber.d("🤖 다운로드 완료!")
                        }
                        override fun onDownloadFailed(errorCode: GenAiException) {
                            Timber.e("🤖 다운로드 실패: $errorCode")
                        }
                    }).await()
                }
                2 -> Timber.d("🤖 모델 다운로드 중... (DOWNLOADING)")
                3 -> Timber.d("🤖 사용 가능! (AVAILABLE)")
            }

            summarizer.close()

        } catch (e: Exception) {
            Timber.e(e, "🤖 AICore 체크 실패")
        }
    }
    }


        // 임시 테스트 코드 - 확인 후 삭제
    fun summarizeText(context: Context, inputText: String) {

        viewModelScope.launch(Dispatchers.IO) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val options = SummarizerOptions.builder(context)
                        //.setInputType(SummarizerOptions.InputType.ARTICLE)
                        .setInputType(SummarizerOptions.InputType.CONVERSATION) // 400자 제한 없음
                        .setOutputType(SummarizerOptions.OutputType.ONE_BULLET)
                        .setLanguage(SummarizerOptions.Language.ENGLISH)
                        .build()

                    val summarizer = Summarization.getClient(options)

                    // 엔진 준비
                    summarizer.prepareInferenceEngine().await()
                    Timber.d("🤖 엔진 준비 완료")

                    // 입력 텍스트 400자 이상이어야 함.
                    val request = SummarizationRequest.builder(
                        "Android is a mobile operating system developed by Google. " +
                                "It is based on the Linux kernel and is designed primarily for touchscreen " +
                                "mobile devices such as smartphones and tablets. "
//                                "Android was first released in 2008 and has since become the most widely used " +
//                                "mobile operating system in the world, with billions of active devices. " +
//                                "The operating system features a large ecosystem of applications available through " +
//                                "the Google Play Store, which contains millions of apps for productivity, entertainment, " +
//                                "communication, and many other purposes. Google regularly releases new versions of Android " +
//                                "with improved features, security updates, and performance enhancements."
                    ).build()

                    val result = summarizer.runInference(request).await()
                    Timber.d("🤖 요약 결과: ${result.summary}")

                    summarizer.close()

                } catch (e: Exception) {
                    Timber.e(e, "🤖 요약 실패")
                }
            }
        }
    }

    //  Prompt API가 S25에서 아직 미지원
//    fun testKeywordExtract() {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val model = Generation.getClient()
//
//                // 상태 체크
//                val status = model.checkStatus()
//                Timber.d("🤖 Prompt API 상태: $status")
//
//                when (status) {
//                    FeatureStatus.UNAVAILABLE -> {
//                        Timber.w("🤖 이 기기는 미지원")
//                        return@launch
//                    }
//                    FeatureStatus.DOWNLOADABLE -> {
//                        Timber.d("🤖 모델 다운로드 중...")
//                        model.download().collect { downloadStatus ->
//                            Timber.d("🤖 다운로드: $downloadStatus")
//                        }
//                    }
//                    FeatureStatus.AVAILABLE -> {
//                        // 엔진 워밍업
//                        model.warmup()
//                        Timber.d("🤖 워밍업 완료")
//
//                        val result = model.generateContent(
//                            "Extract 5 keywords from this text. " +
//                                    "Return only the keywords separated by commas, no explanation.\n\n" +
//                                    "Text: Android is a mobile operating system developed by Google. " +
//                                    "It is based on the Linux kernel and is designed primarily for touchscreen " +
//                                    "mobile devices such as smartphones and tablets. " +
//                                    "Android was first released in 2008 and has since become the most widely used " +
//                                    "mobile operating system in the world."
//                        )
//                        Timber.d("🤖 키워드: ${result.candidates.first()}")
//                    }
//                }
//
//                model.close()
//
//            } catch (e: Exception) {
//                Timber.e(e, "🤖 키워드 추출 실패")
//            }
//        }
//    }

}