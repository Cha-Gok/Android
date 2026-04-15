package com.roro.recorder.domain.usecase

import android.os.ParcelFileDescriptor
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.audio.AudioSource
import com.google.mlkit.genai.speechrecognition.SpeechRecognizer
import com.google.mlkit.genai.speechrecognition.speechRecognizerOptions
import com.google.mlkit.genai.speechrecognition.SpeechRecognition
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerOptions
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerResponse
import com.google.mlkit.genai.speechrecognition.speechRecognizerRequest
import timber.log.Timber
import java.io.File
import java.util.Locale
import javax.inject.Inject

/**
 * 기능 설명:
 * - 음성 파일을 텍스트로 변환하는 UseCase (STT)
 * - WAV 파일을 청크로 분할 후 MLKit SpeechRecognition으로 순차 인식
 * - 청크별 결과를 합쳐 전체 텍스트 반환
 *
 * @author hyeonseo
 * @since 2026. 04. 12.
 */
class TranscribeAudioUseCase @Inject constructor() {

    /**
     * 음성 파일 STT 변환
     * - WAV 파일을 청크로 분할 후 순차적으로 STT 처리
     * - 처리 완료된 청크 파일은 즉시 삭제
     *
     * @param file STT 변환할 WAV 파일
     * @param value 인식 언어 Locale
     * @return 전체 인식된 텍스트
     *
     * @author hyeonseo
     * @since 2026. 04. 12.
     * @modified
     */
    suspend operator fun invoke(file: File, value: Locale): String {
        val chunks = splitWavFileToChunks(file, chunkSeconds = 6)
        val fullText = StringBuilder()

        chunks.forEachIndexed { index, chunkFile ->
            Timber.d("🎤 청크 ${index + 1}/${chunks.size} 처리 중")
            val result = recognizeChunk(chunkFile)
            if (result.isNotBlank()) fullText.append(result).append(" ")
            chunkFile.delete()
        }

        return fullText.toString().trim()
    }

    /**
     * 청크 파일 단위 STT 인식
     * - MLKit SpeechRecognizer로 청크 파일 인식
     * - 인식 불가 상태이거나 실패 시 빈 문자열 반환
     *
     * @param chunkFile 인식할 청크 WAV 파일
     * @return 인식된 텍스트, 실패 시 빈 문자열
     *
     * @author hyeonseo
     * @since 2026. 04. 12.
     * @modified
     */
    private suspend fun recognizeChunk(chunkFile: File): String {
        var speechRecognizer: SpeechRecognizer? = null
        var pfd: ParcelFileDescriptor? = null
        return try {
            val options = speechRecognizerOptions {
                locale = Locale("ko", "KR")
                preferredMode = SpeechRecognizerOptions.Mode.MODE_BASIC
            }
            speechRecognizer = SpeechRecognition.getClient(options)

            if (speechRecognizer.checkStatus() != FeatureStatus.AVAILABLE) return ""

            pfd = ParcelFileDescriptor.open(chunkFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val request = speechRecognizerRequest {
                audioSource = AudioSource.fromPfd(pfd)
            }

            var result = ""
            speechRecognizer.startRecognition(request).collect { response ->
                when (response) {
                    is SpeechRecognizerResponse.FinalTextResponse -> result = response.text
                    is SpeechRecognizerResponse.PartialTextResponse -> Timber.d("🎤 [중간] ${response.text}")
                    is SpeechRecognizerResponse.CompletedResponse -> Timber.d("🎤 [완료]")
                    is SpeechRecognizerResponse.ErrorResponse -> Timber.e("🎤 [에러] ${response.e.message}")
                }
            }
            result
        } catch (e: Exception) {
            Timber.e(e, "🎤 청크 인식 실패")
            ""
        } finally {
            pfd?.close()
            speechRecognizer?.close()
        }
    }

    /**
     * WAV 파일을 청크 단위로 분할
     * - WAV 헤더에서 sampleRate, byteRate, blockAlign 파싱
     * - chunkSeconds 단위로 PCM 데이터 분할 후 각각 WAV 파일로 저장
     *
     * @param file 분할할 WAV 파일
     * @param chunkSeconds 청크 길이 (초)
     * @return 분할된 청크 WAV 파일 목록
     *
     * @author hyeonseo
     * @since 2026. 04. 12.
     * @modified
     */
    private fun splitWavFileToChunks(file: File, chunkSeconds: Int): List<File> {
        val wavBytes = file.readBytes()
        val sampleRate = wavBytes.getIntLE(24)
        val byteRate = wavBytes.getIntLE(28)
        val blockAlign = wavBytes.getShortLE(32)
        val bytesPerChunk = byteRate * chunkSeconds

        val chunks = mutableListOf<File>()
        var offset = 44

        while (offset < wavBytes.size) {
            val end = minOf(offset + bytesPerChunk, wavBytes.size)
            val chunkData = wavBytes.copyOfRange(offset, end)
            val chunkWav = buildWavHeader(chunkData.size, sampleRate, blockAlign) + chunkData
            val chunkFile = File(file.parent, "chunk_${chunks.size}.wav")
            chunkFile.writeBytes(chunkWav)
            chunks.add(chunkFile)
            offset = end
        }
        return chunks
    }

    /**
     * WAV 헤더 생성
     *
     * @param dataSize PCM 데이터 크기 (byte)
     * @param sampleRate 샘플링 레이트 (Hz)
     * @param blockAlign 블록 정렬 크기
     * @return 44바이트 WAV 헤더
     *
     * @author hyeonseo
     * @since 2026. 04. 12.
     * @modified
     */
    private fun buildWavHeader(dataSize: Int, sampleRate: Int, blockAlign: Short): ByteArray {
        val byteRate = sampleRate * blockAlign
        return ByteArray(44).apply {
            set(0, 'R'.code.toByte()); set(1, 'I'.code.toByte())
            set(2, 'F'.code.toByte()); set(3, 'F'.code.toByte())
            putIntLE(4, dataSize + 36)
            set(8, 'W'.code.toByte()); set(9, 'A'.code.toByte())
            set(10, 'V'.code.toByte()); set(11, 'E'.code.toByte())
            set(12, 'f'.code.toByte()); set(13, 'm'.code.toByte())
            set(14, 't'.code.toByte()); set(15, ' '.code.toByte())
            putIntLE(16, 16); putShortLE(20, 1); putShortLE(22, 1)
            putIntLE(24, sampleRate); putIntLE(28, byteRate)
            putShortLE(32, blockAlign.toInt()); putShortLE(34, 16)
            set(36, 'd'.code.toByte()); set(37, 'a'.code.toByte())
            set(38, 't'.code.toByte()); set(39, 'a'.code.toByte())
            putIntLE(40, dataSize)
        }
    }

    private fun ByteArray.getIntLE(offset: Int) =
        (this[offset].toInt() and 0xFF) or ((this[offset+1].toInt() and 0xFF) shl 8) or
                ((this[offset+2].toInt() and 0xFF) shl 16) or ((this[offset+3].toInt() and 0xFF) shl 24)

    private fun ByteArray.getShortLE(offset: Int) =
        ((this[offset].toInt() and 0xFF) or ((this[offset+1].toInt() and 0xFF) shl 8)).toShort()

    private fun ByteArray.putIntLE(offset: Int, value: Int) {
        this[offset] = (value and 0xFF).toByte()
        this[offset+1] = ((value shr 8) and 0xFF).toByte()
        this[offset+2] = ((value shr 16) and 0xFF).toByte()
        this[offset+3] = ((value shr 24) and 0xFF).toByte()
    }

    private fun ByteArray.putShortLE(offset: Int, value: Int) {
        this[offset] = (value and 0xFF).toByte()
        this[offset+1] = ((value shr 8) and 0xFF).toByte()
    }
}