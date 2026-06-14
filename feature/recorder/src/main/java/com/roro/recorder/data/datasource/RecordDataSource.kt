package com.roro.recorder.data.datasource

import android.Manifest
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.math.sqrt

/**
 * 기능 설명:
 * - 음성 녹음을 담당하는 DataSource
 * - AudioRecord를 사용해 PCM 데이터를 수집하고 WAV 파일로 저장
 * - STT 최적화 형식 (16000Hz, Mono, PCM 16bit)
 *
 * @author hyeonseo
 * @since 2026. 04. 12.
 */
class RecordDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var isRecording = false
    private var currentFile: File? = null
    private var isPaused = false

    // 실시간 amplitude (PCM RMS 기반, 0~32767)
    private val _currentAmplitude = AtomicInteger(0)

    /** ViewModel에서 polling해서 읽는 값 */
    fun getMaxAmplitude(): Int = _currentAmplitude.get()

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val TAG = "RecordDataSource"
    }

    fun pauseRecording() {
        isPaused = true
        _currentAmplitude.set(0)
        audioRecord?.stop()
    }

    fun resumeRecording() {
        isPaused = false
        audioRecord?.startRecording()
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

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording(file: File) {

        // 이미 녹음 중이면 먼저 정리
        if (isRecording) {
            isRecording = false
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            recordingThread?.join()
        }

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

        recordingThread = Thread {
            writeAudioToFile(file, bufferSize)
        }.also { it.start() }

        Timber.tag(TAG).d("🎤 녹음 시작: ${file.absolutePath}")
    }

    fun stopRecording(): File {
        if (!isRecording) throw IllegalStateException("녹음 중이 아닙니다.")

        isRecording = false
        _currentAmplitude.set(0)
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        recordingThread?.join()

        Timber.tag(TAG).d("🛑 녹음 종료: ${currentFile?.absolutePath}")

        return currentFile ?: throw IllegalStateException("녹음 파일 없음")
    }

    private fun writeAudioToFile(file: File, bufferSize: Int) {
        val buffer = ByteArray(bufferSize)
        val pcmData = mutableListOf<Byte>()

        while (isRecording) {
            if (isPaused) {
                Thread.sleep(50)
                continue
            }
            val read = audioRecord?.read(buffer, 0, bufferSize) ?: 0
            // Timber.d("🎤 read=$read, rms=${calculateRms(buffer, read)}")  // 확인용

            if (read > 0) {
                pcmData.addAll(buffer.take(read))

                // ✅ PCM 버퍼에서 RMS amplitude 계산 후 업데이트
                val rms = calculateRms(buffer, read)
                _currentAmplitude.set(rms)
            }
        }

        val pcmBytes = pcmData.toByteArray()
        file.outputStream().use { out ->
            out.write(buildWavHeader(pcmBytes.size))
            out.write(pcmBytes)
        }

        Timber.tag(TAG).d("💾 WAV 저장 완료: ${file.length()} bytes")
    }

    /**
     * PCM 16bit LE 버퍼 → RMS amplitude (0~32767)
     */
    private fun calculateRms(buffer: ByteArray, read: Int): Int {
        var sum = 0.0
        var sampleCount = 0

        var i = 0
        while (i + 1 < read) {
            val sample = (buffer[i].toInt() and 0xFF) or (buffer[i + 1].toInt() shl 8)
            val signedSample = sample.toShort().toDouble()
            sum += signedSample * signedSample
            sampleCount++
            i += 2
        }

        if (sampleCount == 0) return 0
        return sqrt(sum / sampleCount).toInt().coerceIn(0, 32767)
    }

    private fun buildWavHeader(dataSize: Int): ByteArray {
        val byteRate = SAMPLE_RATE * 2
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
            putShortLE(20, 1)
            putShortLE(22, 1)
            putIntLE(24, SAMPLE_RATE)
            putIntLE(28, byteRate)
            putShortLE(32, blockAlign.toInt())
            putShortLE(34, 16)
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