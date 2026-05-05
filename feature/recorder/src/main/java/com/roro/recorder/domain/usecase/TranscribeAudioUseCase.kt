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
 *
 * @author hyeonseo
 * @since 2026. 04. 12.
 */
//class TranscribeAudioUseCase @Inject constructor() {
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
    suspend operator fun invoke(file: File, value: Locale): String {
        val chunks = splitWavFileToChunks(file, chunkSeconds = 7)
        val results = mutableListOf<String>()

        chunks.forEachIndexed { index, chunkFile ->
            Timber.d("🎤 청크 ${index + 1}/${chunks.size} 처리 중")
            val result = recognizeChunk(chunkFile)
            if (result.isNotBlank()) results.add(result)  // ✅ 빈 청크는 제외
            chunkFile.delete()
        }

        return results.joinToString("\n")  // ✅ \n으로 구분 (index * 6000ms = startTimeMs)

    }

    /**
     * 청크 파일 단위 STT 인식
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
//    private fun splitWavFileToChunks(
//        file: File,
//        chunkSeconds: Int = 6,
//        overlapSeconds: Int = 1
//    ): List<File> {
//        val wavBytes = file.readBytes()
//        val sampleRate = wavBytes.getIntLE(24)
//        val byteRate = wavBytes.getIntLE(28)
//        val blockAlign = wavBytes.getShortLE(32)
//        val bytesPerChunk = byteRate * chunkSeconds
//        val bytesPerOverlap = byteRate * overlapSeconds
//        val stepBytes = bytesPerChunk - bytesPerOverlap // 한 번에 이동하는 크기
//
//        val chunks = mutableListOf<File>()
//        var offset = 44 // WAV 헤더 스킵
//
//        while (offset < wavBytes.size) {
//            val end = minOf(offset + bytesPerChunk, wavBytes.size)
//            val chunkData = wavBytes.copyOfRange(offset, end)
//            val chunkWav = buildWavHeader(chunkData.size, sampleRate, blockAlign) + chunkData
//            val chunkFile = File(file.parent, "chunk_${chunks.size}.wav")
//            chunkFile.writeBytes(chunkWav)
//            chunks.add(chunkFile)
//            offset += stepBytes
//        }
//        return chunks
//    }

    /**
     * WAV 파일을 무음 구간 기준으로 분할
     * - 무음 구간에서 자름 (자연스러운 끊김)
     * - 무음 없이 maxChunkSeconds 초과 시 강제로 자름
     *
     * @param file WAV 파일
     * @param maxChunkSeconds 최대 청크 길이 (기본 6초)
     * @param silenceThresholdRms 무음 판단 기준 RMS (기본 300, 환경에 따라 조절)
     * @param minSilenceSamples 최소 무음 길이 (기본 0.3초)
     */
    private fun splitWavBySilence(
        file: File,
        maxChunkSeconds: Int = 6,
        silenceThresholdRms: Int = 150,
        minSilenceSamples: Int = -1 // -1이면 sampleRate * 0.3으로 자동 계산
    ): List<File> {
        val wavBytes = file.readBytes()
        val sampleRate = wavBytes.getIntLE(24)
        val byteRate = wavBytes.getIntLE(28)
        val blockAlign = wavBytes.getShortLE(32)

        val pcmData = wavBytes.copyOfRange(44, wavBytes.size) // 헤더 제거
        val maxChunkBytes = byteRate * maxChunkSeconds
        val actualMinSilenceSamples = if (minSilenceSamples == -1) (sampleRate * 0.3).toInt() else minSilenceSamples

        // 16bit mono 기준 2바이트 = 1샘플
        val bytesPerSample = blockAlign.toInt()

        val splitPoints = mutableListOf<Int>() // 자를 바이트 위치
        var silenceStart = -1
        var silenceSampleCount = 0
        var lastSplitByte = 0

        var i = 0
        while (i < pcmData.size - bytesPerSample) {
            // RMS 계산 (16bit little endian)
            val sample = (pcmData[i].toInt() and 0xFF) or (pcmData[i + 1].toInt() shl 8)
            val rms = Math.abs(sample.toShort().toInt())

            if (rms < silenceThresholdRms) {
                // 무음 구간
                if (silenceStart == -1) silenceStart = i
                silenceSampleCount++

                // 최소 무음 길이 이상 && 청크가 너무 작지 않을 때
                if (silenceSampleCount >= actualMinSilenceSamples &&
                    (i - lastSplitByte) > byteRate * 1 // 최소 1초 이상
                ) {
                    splitPoints.add(silenceStart)
                    lastSplitByte = silenceStart
                    silenceStart = -1
                    silenceSampleCount = 0
                }
            } else {
                // 소리 구간 → 무음 카운트 리셋
                silenceStart = -1
                silenceSampleCount = 0
            }

            // 최대 청크 길이 초과 시 강제로 자름
            if ((i - lastSplitByte) >= maxChunkBytes) {
                splitPoints.add(i)
                lastSplitByte = i
                silenceStart = -1
                silenceSampleCount = 0
            }

            i += bytesPerSample
        }

        // 마지막 포인트 추가
        splitPoints.add(pcmData.size)

        // 청크 파일 생성
        val chunks = mutableListOf<File>()
        var offset = 0
        splitPoints.forEach { end ->
            if (end <= offset) return@forEach
            val chunkData = pcmData.copyOfRange(offset, end)

            val chunkDurationSeconds = chunkData.size.toFloat() / byteRate
            if (chunkDurationSeconds < 1f) {
                Timber.d("🎤 청크 너무 짧아서 스킵: ${chunkDurationSeconds}초")
                offset = end
                return@forEach
            }

            val chunkWav = buildWavHeader(chunkData.size, sampleRate, blockAlign) + chunkData
            val chunkFile = File(file.parent, "chunk_${chunks.size}.wav")
            chunkFile.writeBytes(chunkWav)
            chunks.add(chunkFile)
            Timber.d("🎤 청크 ${chunks.size}: ${offset / byteRate}초 ~ ${end / byteRate}초")
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