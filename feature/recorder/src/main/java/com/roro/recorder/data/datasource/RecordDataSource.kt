package com.roro.recorder.data.datasource

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.annotation.RequiresPermission
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
import java.util.UUID

class RecordDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var isRecording = false
    private var currentFile: File? = null

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val TAG = "RecordDataSource"
    }

    fun createAudioFile(folderName: String? = null): File {

        val baseDir = context.getExternalFilesDir(null)
            ?: throw IllegalStateException("저장소 접근 불가")

        if (!baseDir.exists()) baseDir.mkdirs()

        val now = System.currentTimeMillis()
        val safeName = (folderName ?: UUID.randomUUID().toString())
            .replace("/", "_")

        val file = File(baseDir, "${safeName}_$now.wav")

        return try {
            if (!file.exists()) file.createNewFile()

            Timber.d("파일 생성: ${file.absolutePath}")
            file

        } catch (e: Exception) {
            Timber.e(e)
            file
        }
    }

    /**
     * 녹음 시작
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording(file: File) {
        if (isRecording) throw IllegalStateException("이미 녹음 중입니다.")

        currentFile = file

        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize
        )

        audioRecord?.startRecording()
        isRecording = true

        // 별도 스레드에서 PCM 데이터 수집
        recordingThread = Thread {
            writeAudioToFile(file, bufferSize)
        }.also { it.start() }

        Timber.tag(TAG).d("🎤 녹음 시작: ${file.absolutePath}")
    }

    /**
     * 녹음 중지
     */
    fun stopRecording(): File {
        if (!isRecording) throw IllegalStateException("녹음 중이 아닙니다.")

        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        recordingThread?.join() // 스레드 종료 대기

        Timber.tag(TAG).d("🛑 녹음 종료: ${currentFile?.absolutePath}")

        return currentFile ?: throw IllegalStateException("녹음 파일 없음")
    }

    /**
     * PCM 데이터 수집 → WAV 파일로 저장
     */
    private fun writeAudioToFile(file: File, bufferSize: Int) {
        val buffer = ByteArray(bufferSize)
        val pcmData = mutableListOf<Byte>()

        // PCM 데이터 수집
        while (isRecording) {
            val read = audioRecord?.read(buffer, 0, bufferSize) ?: 0
            if (read > 0) {
                pcmData.addAll(buffer.take(read))
            }
        }

        // WAV 파일로 저장 (헤더 + PCM 데이터)
        val pcmBytes = pcmData.toByteArray()
        file.outputStream().use { out ->
            out.write(buildWavHeader(pcmBytes.size))
            out.write(pcmBytes)
        }

        Timber.tag(TAG).d("💾 WAV 저장 완료: ${file.length()} bytes")
    }

    /**
     * WAV 헤더 생성
     */
    private fun buildWavHeader(dataSize: Int): ByteArray {
        val byteRate = SAMPLE_RATE * 2 // 모노 * 16bit(2byte)
        val blockAlign: Short = 2

        return ByteArray(44).apply {
            set(0, 'R'.code.toByte()); set(1, 'I'.code.toByte())
            set(2, 'F'.code.toByte()); set(3, 'F'.code.toByte())
            putIntLE(4, dataSize + 36)
            set(8, 'W'.code.toByte()); set(9, 'A'.code.toByte())
            set(10, 'V'.code.toByte()); set(11, 'E'.code.toByte())
            set(12, 'f'.code.toByte()); set(13, 'm'.code.toByte())
            set(14, 't'.code.toByte()); set(15, ' '.code.toByte())
            putIntLE(16, 16)
            putShortLE(20, 1)          // PCM
            putShortLE(22, 1)          // 모노
            putIntLE(24, SAMPLE_RATE)
            putIntLE(28, byteRate)
            putShortLE(32, blockAlign.toInt())
            putShortLE(34, 16)         // 16bit
            set(36, 'd'.code.toByte()); set(37, 'a'.code.toByte())
            set(38, 't'.code.toByte()); set(39, 'a'.code.toByte())
            putIntLE(40, dataSize)
        }
    }

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
}