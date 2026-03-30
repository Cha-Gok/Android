package com.roro.recorder.presentation

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    // 요약 ai 연결 확인 여부
    fun checkAICore(context: Context) { viewModelScope.launch(Dispatchers.IO){
        try { val options = SummarizerOptions.builder(context)
            .setInputType(SummarizerOptions.InputType.ARTICLE)
            .setOutputType(SummarizerOptions.OutputType.ONE_BULLET)
            .setLanguage(SummarizerOptions.Language.KOREAN)
            .build()

            val summarizer = Summarization.getClient(options)
            val status = summarizer.checkFeatureStatus().get()

//            val statusFuture = summarizer.checkFeatureStatus()
//            val status = statusFuture.get()

            Timber.d("🤖 Summarization 상태: ${status}")
            // 0=UNAVAILABLE, 1=DOWNLOADABLE, 2=DOWNLOADING, 3=AVAILABLE

            summarizer.close()

        } catch (e: Exception) { Timber.e(e, "🤖 AICore 체크 실패") } }
    }



        // 임시 테스트 코드 - 확인 후 삭제
    fun summarizeText(context: Context, inputText: String) {



        viewModelScope.launch(Dispatchers.IO) {
            try {
                val options = SummarizerOptions.builder(context)
                    .setInputType(SummarizerOptions.InputType.ARTICLE)
                    .setOutputType(SummarizerOptions.OutputType.ONE_BULLET)
                    .setLanguage(SummarizerOptions.Language.KOREAN)
                    .build()

                val summarizer = Summarization.getClient(options)

                // 1️⃣ 상태 확인
                val status = summarizer.checkFeatureStatus()

                //Timber.d("🤖 Summarization 상태: ${status.}")
                // 0=UNAVAILABLE, 1=DOWNLOADABLE, 2=DOWNLOADING, 3=AVAILABLE

//                if (status != 3) {
//                    Timber.e("❌ 사용 불가: $status")
//                    return@launch
//                }

                // 2️⃣ 요청 생성
                val request = SummarizationRequest.builder(inputText)
                    .build()

//                summarizer.summarizeText(inputText)
//                    .addOnSuccessListener { summary ->
//                        // 여기서 실제 요약된 텍스트가 나옵니다!
//                        Log.d("AI_DEBUG", "요약 성공: $summary")
//                        // UI 업데이트 등을 여기서 진행하세요.
//                    }
//                    .addOnFailureListener { e ->
//                        // 에러 발생 시 (모델 미설치 등)
//                        Log.e("AI_DEBUG", "요약 실패: ${e.message}")
//                    }



                // 3️⃣ 실행
                val result = summarizer.runInference(request).get()
                //_updateState { RecordState.Success(result.summary) }

                // 4️⃣ 결과
                Timber.d("✅ 요약 결과: ${result.summary}")


                summarizer.close()

            } catch (e: Exception) {
                Timber.e(e, "❌ summarize 실패")
            }
        }
    }

}