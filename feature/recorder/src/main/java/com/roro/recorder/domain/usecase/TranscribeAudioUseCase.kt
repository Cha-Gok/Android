package com.roro.recorder.domain.usecase

import android.content.Context
import android.os.ParcelFileDescriptor
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.audio.AudioSource
import com.google.mlkit.genai.speechrecognition.SpeechRecognizer
import com.google.mlkit.genai.speechrecognition.speechRecognizerOptions
import com.google.mlkit.genai.speechrecognition.SpeechRecognition
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerOptions
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerResponse
import com.google.mlkit.genai.speechrecognition.speechRecognizerRequest
import com.roro.core.datastore.Language
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import java.util.Locale
import javax.inject.Inject

/**
 * 기능 설명:
 * - 음성 파일을 텍스트로 변환하는 UseCase (STT)
 * - WAV 파일을 청크로 분할 후 MLKit SpeechRecognition으로 순차 인식
 * - 청크별 결과를 \n으로 구분해 반환 → index * 6000ms = startTimeMs 계산 가능
 * - 설정언어가 영어인 경우 -> 영어로 stt 진행 및 요약(원래 영어)
 * - 설정 언어가 한국어인 경우 -> 한국어로 stt 진행 및 요약 (+ 요약 결과 영한 번역)
 *
 * @author hyeonseo
 * @since 2026. 04. 12.
 */

// 수정 부분
// gemma 연동 버전으로 변경 필요
// 30초 단위 chunk 로 나누어 stt 진행

// 고민해야 할 부분
// 단락 마무리, 문장 마무리 부분마다 나누어 타임스탬프 저장 -> 스크립트 표시용
// 1번 stt(스크립트용) / 교정 + 요약 + 키워드 추출 -> 스크립트 자체는 정확도 떨어질 수 있으나 프롬프팅 진행하면 괜찮을 수도?
// 2번 stt + 교정(스크립트용) + 요약 + 키워드 추출 -> 요약, stt 부분 프롬프팅 수정. (타임스탬프 보존되게)

class TranscribeAudioUseCase @Inject constructor(
    @ApplicationContext private val context: Context  // ← 추가
) {
    /**
     * 음성 파일 STT 변환
     * - WAV 파일을 청크로 분할 후 순차적으로 STT 처리
     * - 청크 구분자: \n (index * 6000ms = startTimeMs)
     * - 처리 완료된 청크 파일은 즉시 삭제
     *
     * @param file STT 변환할 WAV 파일
     * @param value 인식 언어 Locale
     * @return 청크별 결과를 \n으로 구분한 전체 텍스트
     */
    suspend operator fun invoke(file: File, language: Language): String {

        val locale = language.locale
        val chunks = splitWavFileToChunks(file, chunkSeconds = 7)
        val results = mutableListOf<String>()

        chunks.forEachIndexed { index, chunkFile ->
            Timber.d("🎤 청크 ${index + 1}/${chunks.size} 처리 중 (언어: $locale)")
            val result = recognizeChunk(chunkFile, locale)
            if (result.isNotBlank()) results.add(result)
            chunkFile.delete()
        }

        return results.joinToString("\n")
    }

    /**
     * 청크 파일 단위 STT 인식
     */
    /**
     * 청크 파일 단위 STT 인식
     *
     * @param chunkFile 인식할 WAV 청크 파일
     * @param locale 인식 언어 Locale
     */
    private suspend fun recognizeChunk(chunkFile: File, locale: Locale): String {
        var speechRecognizer: SpeechRecognizer? = null
        var pfd: ParcelFileDescriptor? = null
        return try {
            val options = speechRecognizerOptions {
                this.locale = locale
                preferredMode = SpeechRecognizerOptions.Mode.MODE_BASIC
            }
            speechRecognizer = SpeechRecognition.getClient(options)

            val status = speechRecognizer.checkStatus()
            Timber.d("🎤 STT 상태: $status") // ✅ 여기

            if (status != FeatureStatus.AVAILABLE) {
                Timber.d("🎤 STT 사용 불가 → 상태: $status") // ✅ 여기
                return ""
            }

            Timber.d("🎤 청크 파일 크기: ${chunkFile.length()}bytes") // ✅ 여기

            pfd = ParcelFileDescriptor.open(chunkFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val request = speechRecognizerRequest {
                audioSource = AudioSource.fromPfd(pfd)
            }

            Timber.d("🎤 인식 시작") // ✅ 여기
            var result = ""
            speechRecognizer.startRecognition(request).collect { response ->
                Timber.d("🎤 응답: $response") // ✅ 여기
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