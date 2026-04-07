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
import dagger.hilt.android.internal.Contexts.getApplication
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

import kotlinx.coroutines.tasks.await

@HiltViewModel
class RecordViewModel @Inject constructor(
    //private val recordDataSource: RecordDataSource,
    private val recordDataSource: RecordDataSource,
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
    @SuppressLint("MissingPermission")
    fun startRecording(folderName: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = recordDataSource.createAudioFile(folderName)
                Timber.tag(TAG).d("📁 파일 생성됨: ${file.absolutePath}")
                currentFile = file
                recordDataSource.startRecording(file) // ✅ 주석 해제
                _state.value = RecordState.Recording
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "❌ 녹음 시작 실패")
                _state.value = RecordState.Error(e.message ?: "녹음 시작 실패")
            }
        }
    }

    fun stopRecording() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = recordDataSource.stopRecording()
                Timber.tag(TAG).d("🛑 녹음 종료, STT 시작")
                _state.value = RecordState.Processing

                // File 기반으로 직접 청크 분할 STT 호출
                startTranscribeFromFile(file)

            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "❌ 녹음 종료 실패")
                _state.value = RecordState.Error(e.message ?: "녹음 종료 실패")
            }
        }
    }

    // Uri 대신 File 직접 받는 버전 추가
    fun startTranscribeFromFile(file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _sttResult.value = "인식 중..."
                val recordingStart = System.currentTimeMillis()

                val chunks = splitWavFileToChunks(file, chunkSeconds = 6)
                Timber.tag(TAG).d("🎤 총 ${chunks.size}개 청크로 분할")

                val fullText = StringBuilder()

                chunks.forEachIndexed { index, chunkFile ->
                    Timber.tag(TAG).d("🎤 청크 ${index + 1}/${chunks.size} 처리 중")
                    val result = recognizeChunkFromFile(chunkFile)
                    if (result.isNotBlank()) {
                        fullText.append(result).append(" ")
                        _sttResult.value = fullText.toString().trim()
                    }
                    chunkFile.delete()
                }

                val sttText = fullText.toString().trim()
                val durationSec = (System.currentTimeMillis() - recordingStart) / 1000.0

                // ✅ STT 완료 → DB 저장
                repository.saveRecording(
                    audioFile = file,
                    durationSec = durationSec,
                    sttText = sttText
                )

                Timber.tag(TAG).d("🎤 STT + DB 저장 완료")
                _state.value = RecordState.Success(sttText)


            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "🎤 STT 실패")
                _sttResult.value = "에러: ${e.message}"
                _state.value = RecordState.Error(e.message ?: "STT 실패")
            }
        }
    }

    private suspend fun recognizeChunkFromFile(chunkFile: File): String {
        var speechRecognizer: SpeechRecognizer? = null
        var pfd: ParcelFileDescriptor? = null
        return try {
            val options = speechRecognizerOptions {
                locale = Locale("ko", "KR")
                preferredMode = SpeechRecognizerOptions.Mode.MODE_BASIC
            }
            speechRecognizer = SpeechRecognition.getClient(options)

            val status = speechRecognizer.checkStatus()
            if (status != FeatureStatus.AVAILABLE) {
                Timber.tag(TAG).w("🎤 모델 미준비: $status")
                return ""
            }

            pfd = ParcelFileDescriptor.open(chunkFile, ParcelFileDescriptor.MODE_READ_ONLY)

            val request = speechRecognizerRequest {
                audioSource = AudioSource.fromPfd(pfd)
            }

            var result = ""
            speechRecognizer.startRecognition(request).collect { response ->
                when (response) {
                    is SpeechRecognizerResponse.FinalTextResponse -> {
                        Timber.tag(TAG).d("🎤 [최종] ${response.text}")
                        result = response.text
                    }
                    is SpeechRecognizerResponse.PartialTextResponse ->
                        Timber.tag(TAG).d("🎤 [중간] ${response.text}")
                    is SpeechRecognizerResponse.CompletedResponse ->
                        Timber.tag(TAG).d("🎤 [완료]")
                    is SpeechRecognizerResponse.ErrorResponse ->
                        Timber.tag(TAG).e("🎤 [에러] ${response.e.message}")
                }
            }
            result
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "🎤 청크 인식 실패")
            ""
        } finally {
            pfd?.close()
            speechRecognizer?.close()
        }
    }

    // File 직접 받는 청크 분할 (context 불필요)
    private fun splitWavFileToChunks(file: File, chunkSeconds: Int): List<File> {
        val wavBytes = file.readBytes()

        val sampleRate = wavBytes.getIntLE(24)
        val byteRate = wavBytes.getIntLE(28)
        val blockAlign = wavBytes.getShortLE(32)
        val headerSize = 44
        val bytesPerChunk = byteRate * chunkSeconds

        val chunks = mutableListOf<File>()
        var offset = headerSize

        while (offset < wavBytes.size) {
            val end = minOf(offset + bytesPerChunk, wavBytes.size)
            val chunkData = wavBytes.copyOfRange(offset, end)
            val chunkWav = buildWavHeader(chunkData.size, sampleRate, blockAlign) + chunkData

            val chunkFile = File(file.parent, "chunk_${chunks.size}.wav")
            chunkFile.writeBytes(chunkWav)
            chunks.add(chunkFile)
            offset = end
        }

        Timber.tag(TAG).d("🎤 분할 완료: ${chunks.size}개, 각 ${chunkSeconds}초")
        return chunks
    }



    fun checkSTT() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val options = speechRecognizerOptions {
                    //locale = Locale.ENGLISH
                    locale = Locale("ko", "KR")
                    preferredMode = SpeechRecognizerOptions.Mode.MODE_BASIC
                }

                val speechRecognizer = SpeechRecognition.getClient(options)
                val status = speechRecognizer.checkStatus()
                Timber.d("🎤 STT 상태: $status")

                when (status) {
                    FeatureStatus.UNAVAILABLE -> Timber.w("🎤 이 기기 미지원 (UNAVAILABLE)")
                    FeatureStatus.DOWNLOADABLE -> {
                        Timber.tag(TAG).d("🎤 다운로드 필요 (DOWNLOADABLE)")
                        speechRecognizer.download().collect { downloadStatus ->
                            when (downloadStatus) {
                                is DownloadStatus.DownloadStarted ->
                                    Timber.tag(TAG).d("🎤 다운로드 시작: ${downloadStatus.bytesToDownload / 1024 / 1024}MB")  // ✅ bytesToDownload
                                is DownloadStatus.DownloadProgress ->
                                    Timber.tag(TAG).d("🎤 다운로드 중: ${downloadStatus.totalBytesDownloaded / 1024 / 1024}MB")   // ✅ bytesDownloaded
                                is DownloadStatus.DownloadCompleted ->
                                    Timber.tag(TAG).d("🎤 다운로드 완료!")
                                is DownloadStatus.DownloadFailed ->
                                    Timber.tag(TAG).d("🎤 다운로드 실패: ${downloadStatus}")                           // ✅ error
                            }
                        }

                        val newStatus = speechRecognizer.checkStatus()
                        Timber.tag(TAG).d("🎤 다운로드 후 상태: $newStatus")
                    }
                    FeatureStatus.DOWNLOADING -> Timber.tag(TAG).d("🎤 다운로드 중... (DOWNLOADING)")
                    FeatureStatus.AVAILABLE -> Timber.tag(TAG).d("🎤 사용 가능! (AVAILABLE)")
                }

                speechRecognizer.close()

            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "🎤 STT 체크 실패")
            }
        }
    }

    // 여기서부터는 stt

    // STT 결과 state 추가
    private val _sttResult = MutableStateFlow("")
    val sttResult: StateFlow<String> = _sttResult.asStateFlow()

//    fun startTranscribeFromUri(uri: Uri, context: Context) {
//        viewModelScope.launch(Dispatchers.IO) {
//            var speechRecognizer: SpeechRecognizer? = null
//            var pfd: ParcelFileDescriptor? = null
//            try {
//                val options = speechRecognizerOptions {
//                    locale = Locale("ko", "KR")
//                    preferredMode = SpeechRecognizerOptions.Mode.MODE_BASIC
//                }
//
//                speechRecognizer = SpeechRecognition.getClient(options)
//
//                val status = speechRecognizer.checkStatus()
//                if (status != FeatureStatus.AVAILABLE) {
//                    Timber.tag(TAG).w("🎤 모델 미준비: $status")
//                    return@launch
//                }
//
//                pfd = context.contentResolver.openFileDescriptor(uri, "r")
//                    ?: run {
//                        Timber.tag(TAG).e("🎤 PFD 열기 실패")
//                        return@launch
//                    }
//
//                val request = speechRecognizerRequest {
//                    audioSource = AudioSource.fromPfd(pfd)
//                }
//
//                Timber.tag(TAG).d("🎤 STT 시작: $uri")
//                _sttResult.value = "인식 중..."
//
//                speechRecognizer.startRecognition(request).collect { response ->
//                    Timber.tag(TAG).d("🎤 response: $response")
//                    when (response) {
//                        is SpeechRecognizerResponse.FinalTextResponse -> {
//                            Timber.tag(TAG).d("🎤 [최종] ${response.text}")
//                            _sttResult.value = response.text
//                        }
//                        is SpeechRecognizerResponse.PartialTextResponse -> {
//                            Timber.tag(TAG).d("🎤 [중간] ${response.text}")
//                            _sttResult.value = response.text
//                        }
//                        is SpeechRecognizerResponse.CompletedResponse -> {
//                            Timber.tag(TAG).d("🎤 [완료]")
//                        }
//                        is SpeechRecognizerResponse.ErrorResponse -> {
//                            Timber.tag(TAG).e("🎤 [에러] ${response.e.message}")
//                            _sttResult.value = "에러: ${response.e.message}"
//                        }
//                    }
//                }
//
//                Timber.tag(TAG).d("🎤 STT 완료")
//
//            } catch (e: Exception) {
//                Timber.tag(TAG).e(e, "🎤 STT 실패")
//                _sttResult.value = "STT 실패: ${e.message}"
//            } finally {
//                pfd?.close()
//                speechRecognizer?.close()
//            }
//        }
//    }

    // --------------------------------------------------------
    // 청크 나눠서 stt
    fun startTranscribeFromUri(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _sttResult.value = "인식 중..."

                val chunks = splitWavToChunks(uri, context, chunkSeconds = 6)
                Timber.tag(TAG).d("🎤 총 ${chunks.size}개 청크로 분할")

                val fullText = StringBuilder()

                chunks.forEachIndexed { index, chunkFile ->
                    Timber.tag(TAG).d("🎤 청크 ${index + 1}/${chunks.size} 처리 중")
                    val result = recognizeChunk(chunkFile, context)
                    if (result.isNotBlank()) {
                        fullText.append(result).append(" ")
                        _sttResult.value = fullText.toString().trim()
                    }
                    chunkFile.delete()
                }

                Timber.tag(TAG).d("🎤 전체 STT 완료: ${fullText}")

            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "🎤 STT 실패")
                _sttResult.value = "에러: ${e.message}"
            }
        }
    }

    private suspend fun recognizeChunk(chunkFile: File, context: Context): String {
        var speechRecognizer: SpeechRecognizer? = null
        var pfd: ParcelFileDescriptor? = null
        return try {
            val options = speechRecognizerOptions {
                locale = Locale("ko", "KR")
                preferredMode = SpeechRecognizerOptions.Mode.MODE_BASIC
            }
            speechRecognizer = SpeechRecognition.getClient(options)

            val status = speechRecognizer.checkStatus()
            if (status != FeatureStatus.AVAILABLE) {
                Timber.tag(TAG).w("🎤 모델 미준비: $status")
                return ""
            }

            pfd = ParcelFileDescriptor.open(chunkFile, ParcelFileDescriptor.MODE_READ_ONLY)

            val request = speechRecognizerRequest {
                audioSource = AudioSource.fromPfd(pfd)
            }

            var result = ""
            speechRecognizer.startRecognition(request).collect { response ->
                when (response) {
                    is SpeechRecognizerResponse.FinalTextResponse -> {
                        Timber.tag(TAG).d("🎤 [최종] ${response.text}")
                        result = response.text
                    }
                    is SpeechRecognizerResponse.PartialTextResponse -> {
                        Timber.tag(TAG).d("🎤 [중간] ${response.text}")
                    }
                    is SpeechRecognizerResponse.CompletedResponse -> {
                        Timber.tag(TAG).d("🎤 [완료]")
                    }
                    is SpeechRecognizerResponse.ErrorResponse -> {
                        Timber.tag(TAG).e("🎤 [에러] ${response.e.message}")
                    }
                }
            }
            result

        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "🎤 청크 인식 실패")
            ""
        } finally {
            pfd?.close()
            speechRecognizer?.close()
        }
    }

    private fun splitWavToChunks(uri: Uri, context: Context, chunkSeconds: Int): List<File> {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("파일을 열 수 없음")

        val wavBytes = inputStream.readBytes()
        inputStream.close()

        // WAV 헤더 파싱 (44바이트)
        val sampleRate = wavBytes.getIntLE(24)       // offset 24: 샘플레이트
        val byteRate = wavBytes.getIntLE(28)          // offset 28: 바이트레이트
        val blockAlign = wavBytes.getShortLE(32)      // offset 32: 블록 정렬
        val headerSize = 44

        val bytesPerChunk = byteRate * chunkSeconds
        val dataBytes = wavBytes.size - headerSize
        val header = wavBytes.copyOfRange(0, headerSize)

        val chunks = mutableListOf<File>()
        var offset = headerSize

        while (offset < wavBytes.size) {
            val end = minOf(offset + bytesPerChunk, wavBytes.size)
            val chunkData = wavBytes.copyOfRange(offset, end)

            // 청크용 WAV 헤더 재생성
            val chunkWav = buildWavHeader(chunkData.size, sampleRate, blockAlign) + chunkData

            val chunkFile = File(context.cacheDir, "chunk_${chunks.size}.wav")
            chunkFile.writeBytes(chunkWav)
            chunks.add(chunkFile)

            offset = end
        }

        Timber.tag(TAG).d("🎤 분할 완료: ${chunks.size}개, 각 ${chunkSeconds}초")
        return chunks
    }

    // WAV 헤더 생성 (44바이트)
    private fun buildWavHeader(dataSize: Int, sampleRate: Int, blockAlign: Short): ByteArray {
        val totalSize = dataSize + 36
        val byteRate = sampleRate * blockAlign

        return ByteArray(44).apply {
            // RIFF
            set(0, 'R'.code.toByte()); set(1, 'I'.code.toByte())
            set(2, 'F'.code.toByte()); set(3, 'F'.code.toByte())
            putIntLE(4, totalSize)
            // WAVE
            set(8, 'W'.code.toByte()); set(9, 'A'.code.toByte())
            set(10, 'V'.code.toByte()); set(11, 'E'.code.toByte())
            // fmt
            set(12, 'f'.code.toByte()); set(13, 'm'.code.toByte())
            set(14, 't'.code.toByte()); set(15, ' '.code.toByte())
            putIntLE(16, 16)           // PCM chunk size
            putShortLE(20, 1)          // PCM format
            putShortLE(22, 1)          // 모노
            putIntLE(24, sampleRate)
            putIntLE(28, byteRate)
            putShortLE(32, blockAlign.toInt())
            putShortLE(34, 16)         // 16bit
            // data
            set(36, 'd'.code.toByte()); set(37, 'a'.code.toByte())
            set(38, 't'.code.toByte()); set(39, 'a'.code.toByte())
            putIntLE(40, dataSize)
        }
    }

    // ByteArray 확장 함수
    private fun ByteArray.getIntLE(offset: Int): Int =
        (this[offset].toInt() and 0xFF) or
                ((this[offset + 1].toInt() and 0xFF) shl 8) or
                ((this[offset + 2].toInt() and 0xFF) shl 16) or
                ((this[offset + 3].toInt() and 0xFF) shl 24)

    private fun ByteArray.getShortLE(offset: Int): Short =
        ((this[offset].toInt() and 0xFF) or
                ((this[offset + 1].toInt() and 0xFF) shl 8)).toShort()

    private fun ByteArray.putIntLE(offset: Int, value: Int) {
        this[offset] = (value and 0xFF).toByte()
        this[offset + 1] = ((value shr 8) and 0xFF).toByte()
        this[offset + 2] = ((value shr 16) and 0xFF).toByte()
        this[offset + 3] = ((value shr 24) and 0xFF).toByte()
    }

    private fun ByteArray.putShortLE(offset: Int, value: Int) {
        this[offset] = (value and 0xFF).toByte()
        this[offset + 1] = ((value shr 8) and 0xFF).toByte()
    }




    // ------------------------------------------------------------


    // ViewModel 공개 함수 (Screen에서 호출)
    fun startTranscribe(audioFile: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _transcribeState.value = TranscribeState.Loading
                val result = transcribeFile(audioFile) // ✅ 아래 suspend 함수 호출
                _transcribeState.value = TranscribeState.Success(result)
            } catch (e: Exception) {
                _transcribeState.value = TranscribeState.Error(e.message ?: "오류")
            }
        }
    }

    // 실제 변환 로직 (내부 suspend 함수)
    private suspend fun transcribeFile(audioFile: File): String {
        val options = speechRecognizerOptions {
            locale = Locale("ko", "KR")
            preferredMode = SpeechRecognizerOptions.Mode.MODE_BASIC
        }
        val speechRecognizer = SpeechRecognition.getClient(options)

        val pfd = ParcelFileDescriptor.open(audioFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val audioSrc = AudioSource.fromPfd(pfd)

        val request = SpeechRecognizerRequest.builder().apply {
            this.audioSource = audioSrc
        }.build()

        var resultText = ""

        speechRecognizer.startRecognition(request).collect { response ->
            when (response) {
                is SpeechRecognizerResponse.PartialTextResponse ->
                    Timber.tag(TAG).d("🎤 중간 결과: ${response.text}")
                is SpeechRecognizerResponse.FinalTextResponse ->
                    resultText = response.text
                is SpeechRecognizerResponse.CompletedResponse ->
                    Timber.tag(TAG).d("🎤 인식 완료")
                is SpeechRecognizerResponse.ErrorResponse ->
                    Timber.tag(TAG).e("🎤 인식 오류: $response")
            }
        }

        pfd.close()
        speechRecognizer.close()
        return resultText
    }

    // 요약 ai 연결 확인 여부
    fun checkAICore(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val options = SummarizerOptions.builder(context)
                    .setInputType(SummarizerOptions.InputType.ARTICLE)
                    .setOutputType(SummarizerOptions.OutputType.ONE_BULLET)
                    .setLanguage(SummarizerOptions.Language.ENGLISH)
                    //.setLanguage(2) //
                    .build()

                val summarizer = Summarization.getClient(options)
                //val summarizer = Summarization.getClient(options)

                // ✅ Task를 코루틴으로 변환 (블로킹 .get() 대신)
                val status = summarizer.checkFeatureStatus().await()
                // 0=UNAVAILABLE, 1=DOWNLOADABLE, 2=DOWNLOADING, 3=AVAILABLE

                Timber.d("🤖 Summarization 상태: $status")

                when (status) {
                    0 -> Timber.tag(TAG).w("🤖 이 기기는 Summarization 미지원 (UNAVAILABLE)")
                    1 -> {
                        Timber.d("🤖 모델 다운로드 필요 (DOWNLOADABLE)")
                        summarizer.downloadFeature(object : DownloadCallback {
                            override fun onDownloadStarted(bytesToDownload: Long) {
                                Timber.tag(TAG).d("🤖 다운로드 시작: ${bytesToDownload}bytes")
                            }

                            override fun onDownloadProgress(bytesDownloaded: Long) {
                                Timber.tag(TAG).d("🤖 다운로드 중: ${bytesDownloaded}bytes")
                            }

                            override fun onDownloadCompleted() {
                                Timber.tag(TAG).d("🤖 다운로드 완료!")
                            }

                            override fun onDownloadFailed(errorCode: GenAiException) {
                                Timber.tag(TAG).e("🤖 다운로드 실패: $errorCode")
                            }
                        }).await()

                        // ✅ 다운로드 후 상태 재확인
                        val newStatus = summarizer.checkFeatureStatus().await()
                        Timber.tag(TAG).d("🤖 다운로드 후 상태: $newStatus")
                    }
                    2 -> Timber.tag(TAG).d("🤖 모델 다운로드 중... (DOWNLOADING)")
                    3 -> Timber.tag(TAG).d("🤖 사용 가능! (AVAILABLE)")
                    }

                summarizer.close()

            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "🤖 AICore 체크 실패")
            }
        }
    }


    sealed class SummarizeState {
        object Idle : SummarizeState()
        object Loading : SummarizeState()
        data class Success(val summary: String) : SummarizeState()
        data class Error(val message: String) : SummarizeState()
    }

    private val _summarizeState = MutableStateFlow<SummarizeState>(SummarizeState.Idle)
    val summarizeState = _summarizeState.asStateFlow()

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
                        //.setLanguage(2)
                        .build()

                    val summarizer = Summarization.getClient(options)
                    // 엔진 준비
                            summarizer.prepareInferenceEngine().await()
                    Timber.tag(TAG).d("🤖 엔진 준비 완료")

                    // 입력 텍스트 400자 이상이어야 함.
                    val request = SummarizationRequest.builder(inputText).build()
                    val result = summarizer.runInference(request).await()
                    Timber.tag(TAG).d("🤖 요약 결과: ${result.summary}")

                    summarizer.close()

                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "🤖 요약 실패")
                }
            }
        }
    }


    sealed class TranscribeState {
        object Idle : TranscribeState()
        object Loading : TranscribeState()
        data class Success(val text: String) : TranscribeState()
        data class Error(val message: String) : TranscribeState()
    }

    private val _transcribeState = MutableStateFlow<TranscribeState>(TranscribeState.Idle)
    val transcribeState = _transcribeState.asStateFlow()

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
//                        Timber.tag(TAG).d("🤖 키워드: ${result.candidates.first()}")
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



    // 0331 재확인 필요
//    fun startTranscribeFromUri(uri: Uri, context: Context) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                _transcribeState.value = TranscribeState.Loading
//
//                // Uri → ParcelFileDescriptor로 직접 변환 (File 변환 불필요)
//                val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@launch
//                val result = transcribeFromPfd(pfd)
//
//                _transcribeState.value = TranscribeState.Success(result)
//            } catch (e: Exception) {
//                _transcribeState.value = TranscribeState.Error(e.message ?: "오류")
//            }
//        }
//    }
//
//    private suspend fun transcribeFromPfd(pfd: ParcelFileDescriptor): String {
//        val options = speechRecognizerOptions {
//            locale = Locale("ko", "KR")
//            preferredMode = SpeechRecognizerOptions.Mode.MODE_BASIC
//        }
//        val speechRecognizer = SpeechRecognition.getClient(options)
//        val audioSource = AudioSource.fromPfd(pfd)
//
//        val request = SpeechRecognizerRequest.builder().apply {
//            this.audioSource = audioSource
//        }.build()
//
//        var resultText = ""
//        speechRecognizer.startRecognition(request).collect { response ->
//            when (response) {
//                is SpeechRecognizerResponse.FinalTextResponse -> resultText = response.text
//                is SpeechRecognizerResponse.PartialTextResponse -> Timber.d("🎤 중간: ${response.text}")
//                is SpeechRecognizerResponse.CompletedResponse -> Timber.d("🎤 완료")
//                is SpeechRecognizerResponse.ErrorResponse -> Timber.e("🎤 오류: $response")
//            }
//        }
//
//        pfd.close()
//        speechRecognizer.close()
//        return resultText
//    }

    // 0401 번역 관련 코드
    private val _translateState = MutableStateFlow<TranslateState>(TranslateState.Idle)
    val translateState = _translateState.asStateFlow()

    sealed class TranslateState {
        object Idle : TranslateState()
        object Loading : TranslateState()
        data class Success(val text: String) : TranslateState()
        data class Error(val message: String) : TranslateState()
    }

    /**
     * 번역 모델 상태 확인 + 자동 다운로드 (checkSTT / checkAICore 패턴 동일)
     */
    fun checkTranslateModel() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.KOREAN)
                    .setTargetLanguage(TranslateLanguage.ENGLISH)
                    .build()
                val translator = Translation.getClient(options)

                val conditions = DownloadConditions.Builder()
                    .requireWifi()
                    .build()

                translator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener {
                        Timber.tag(TAG).d("🌐 번역 모델 준비 완료!")
                    }
                    .addOnFailureListener { e ->
                        Timber.tag(TAG).e(e, "🌐 번역 모델 다운로드 실패")
                    }

                translator.close()

            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "🌐 번역 모델 체크 실패")
            }
        }
    }

    /**
     * 한국어 → 영어 번역 후 요약까지 연결
     * transcribeState.Success 이후 호출
     */
    fun translateAndSummarize(context: Context, koreanText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _translateState.value = TranslateState.Loading
                Timber.tag(TAG).d("🌐 번역 시작: ${koreanText.take(50)}...")

                //val englishText = translateKoToEn(koreanText) // 한국어 -> 영어
                val englishText = translateEnToKo(koreanText) // 영어 -> 한국어

                Timber.tag(TAG).d("🌐 번역 완료: ${englishText.take(50)}...")

                _translateState.value = TranslateState.Success(englishText)

                // 바로 요약으로 연결
                summarizeText(context, englishText)

            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "🌐 번역 실패")
                _translateState.value = TranslateState.Error(e.message ?: "번역 실패")
            }
        }
    }

    /**
     * 실제 번역 로직 - suspend (transcribeFile 패턴과 동일)
     */
    private suspend fun translateKoToEn(text: String): String {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.KOREAN)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        val translator = Translation.getClient(options)

        return try {
            // 모델 다운로드 확인 (이미 있으면 즉시 통과)
            val conditions = DownloadConditions.Builder().requireWifi().build()
            translator.downloadModelIfNeeded(conditions).await()

            // 번역 실행
            translator.translate(text).await()
        } finally {
            translator.close()
        }
    }

    private suspend fun translateEnToKo(text: String): String {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.KOREAN)
            .build()
        val translator = Translation.getClient(options)

        return try {
            // 모델 다운로드 확인 (이미 있으면 즉시 통과)
            val conditions = DownloadConditions.Builder().requireWifi().build()
            translator.downloadModelIfNeeded(conditions).await()

            // 번역 실행
            translator.translate(text).await()
        } finally {
            translator.close()
        }
    }



}