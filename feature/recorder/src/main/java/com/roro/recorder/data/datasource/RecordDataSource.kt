package com.roro.recorder.data.datasource

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.ParcelFileDescriptor
//import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
//import com.google.mlkit.genai.common.audio.AudioSource
//import com.google.mlkit.genai.speechrecognition.SpeechRecognizerRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
//import com.google.mlkit.genai.speechrecognition.*
//import com.google.mlkit.genai.speechrecognition.SpeechRecognition
//import com.google.mlkit.genai.speechrecognition.SpeechRecognizerOptions
import java.util.Locale

class RecordDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var isRecording = false

    /**
     * 파일 생성 -> RecorderLocalFileDataSource
     */
    private fun createOutputFile(): File {
        val fileName = "record_${System.currentTimeMillis()}.m4a"
        return File(context.getExternalFilesDir(null), fileName)
    }

    /**
     * 녹음 시작
     */
    fun startRecording(file: File) {  // 반환값도 필요없음
        if (isRecording) throw IllegalStateException("이미 녹음 중입니다.")

        currentFile = file  // ← 받은 파일 그대로 사용

        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            isRecording = true
        } catch (e: Exception) {
            releaseRecorder()
            throw e
        }
    }

    /**
     * 녹음 중지
     */
    fun stopRecording(): File {
        if (!isRecording) {
            throw IllegalStateException("녹음 중이 아닙니다.")
        }

        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            Timber.e(e, "녹음 stop 실패 (너무 짧은 녹음 등)")
        } finally {
            releaseRecorder()
        }

        isRecording = false
        Timber.d("녹음 종료")

        return currentFile ?: throw IllegalStateException("녹음 파일 없음")
    }

    private fun releaseRecorder() {
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            mediaRecorder = null
        }
    }

    /**
     * STT (ML Kit / GenAI)
     */
//    suspend fun getTranscript(audioFile: File): String = withContext(Dispatchers.IO) {
//        val options = speechRecognizerOptions {
//            locale = Locale.KOREAN
//            preferredMode = SpeechRecognizerOptions.Mode.MODE_BASIC
//        }
//        val speechRecognizer = SpeechRecognition.getClient(options)
//
//        try {
//            val status = speechRecognizer.checkStatus()
//            if (status == FeatureStatus.DOWNLOADABLE) {
//                speechRecognizer.download().collect { downloadStatus ->
//                    if (downloadStatus is DownloadStatus.DownloadFailed) {
//                        throw IllegalStateException("모델 다운로드 실패")
//                    }
//                }
//            } else if (status != FeatureStatus.AVAILABLE) {
//                throw IllegalStateException("STT 모델 사용 불가: $status")
//            }
//
//            // 파일을 “스트리밍처럼” 넣는 방식
//            val pfd = ParcelFileDescriptor.open(audioFile, ParcelFileDescriptor.MODE_READ_ONLY)
////            val request = speechRecognizerRequest {
////                audioSource = AudioSource.fromPfd(pfd)  // ← 이렇게
////            }
////
////            val result = StringBuilder()
////            speechRecognizer.startRecognition(request).collect { response ->
////                when (response) {
////                    is SpeechRecognizerResponse.FinalTextResponse -> {
////                        result.append(response.text)
////                    }
////                    is SpeechRecognizerResponse.PartialTextResponse -> {
////                        // 중간 결과 (필요하면 로깅)
////                        Timber.d("STT 부분 결과: ${response.text}")
////                    }
////                    is SpeechRecognizerResponse.CompletedResponse -> {
////                        // 인식 완료 신호
////                        Timber.d("STT 완료")
////                    }
////                    is SpeechRecognizerResponse.ErrorResponse -> {
////                        throw IllegalStateException("STT 에러: ${response}")
////                    }
////                }
////            }
//
//            //return@withContext result.toString()
//
//            result.toString()
//
//        } finally {
//            speechRecognizer.stopRecognition()
//            speechRecognizer.close()
//        }
//    }

    /**
     * 요약 (GenAI)
     */
    suspend fun summarize(text: String): String = withContext(Dispatchers.IO) {
        try {
            Timber.d("요약 시작")

            // TODO: ML Kit GenAI 요약 API
            "요약된 텍스트"

        } catch (e: Exception) {
            Timber.e(e, "요약 실패")
            throw e
        }
    }


}