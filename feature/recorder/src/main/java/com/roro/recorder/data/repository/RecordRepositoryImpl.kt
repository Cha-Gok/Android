package com.roro.recorder.data.repository

import android.Manifest
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import com.google.mlkit.genai.speechrecognition.SpeechRecognition
import com.google.mlkit.genai.speechrecognition.SpeechRecognizer
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerOptions
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerRequest
import com.roro.recorder.domain.repository.RecordRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import javax.inject.Inject

class RecordRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : RecordRepository {

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var sttJob: Job? = null

    private var isRecording = false
    private var isPaused = false

    private var audioFilePath: String = ""
    private var startTime: Long = 0L
    private var duration: Double = 0.0

    private val _transcript = MutableStateFlow("")
    private var finalText = StringBuilder()

    private var sttClient: SpeechRecognizer? = null

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override suspend fun startRecording(fileName: String) {

        val file = File(context.filesDir, "$fileName.pcm") // 🔥 PCM으로 저장
        audioFilePath = file.absolutePath
        startTime = System.currentTimeMillis()

        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        // 🎤 AudioRecord 생성
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        // 🤖 STT 초기화
        val options = SpeechRecognizerOptions.builder()
            .locale(Locale.KOREAN)
            .build()

        sttClient = SpeechRecognition.getClient(options)

        // 모델 다운로드 (최초 1회)
        sttClient?.download()?.await()

        isRecording = true
        isPaused = false
        audioRecord?.startRecording()

        // 🎤 녹음 + 파일 + STT
        recordingJob = scope.launch {
            FileOutputStream(file).use { fos ->
                val buffer = ByteArray(bufferSize)

                while (isRecording) {
                    if (!isPaused) {
                        val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0

                        if (read > 0) {
                            // 1️⃣ 파일 저장
                            fos.write(buffer, 0, read)

                            // 2️⃣ STT 전달 (복사 필수🔥)
                            val chunk = buffer.copyOf(read)
                            sttClient?.processAudio(chunk)
                        }
                    }
                }
            }
        }

        // 🧠 STT 결과 수신 (비동기)
        val request = SpeechRecognizerRequest.builder().build()

        sttJob = scope.launch {
            sttClient?.startRecognition(request)?.collect { response ->

                val text = response.text ?: return@collect

                // 👉 partial + final 구분 없이 누적
                _transcript.value = text
                finalText.append(" ").append(text)

                Timber.d("STT: $text")
            }
        }
    }

    override suspend fun pauseRecording() {
        isPaused = true
    }

    override suspend fun resumeRecording() {
        isPaused = false
    }

    override suspend fun finishRecording() {

        isRecording = false
        isPaused = false

        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        recordingJob?.join()

        // 🔥 STT 종료 대기
        delay(300) // 약간 대기 (실전 중요)

        sttClient?.close()
        sttClient = null

        sttJob?.cancel()

        duration = (System.currentTimeMillis() - startTime) / 1000.0

        Timber.d("FINAL TEXT: $finalText")
    }

    override fun observeTranscript(): Flow<String> = _transcript

    override fun getAudioFilePath(): String = audioFilePath

    override fun getDuration(): Double = duration

    override fun getFinalText(): String = finalText.toString()
}