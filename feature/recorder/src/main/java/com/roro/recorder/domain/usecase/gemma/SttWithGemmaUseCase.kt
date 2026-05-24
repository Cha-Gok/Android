package com.roro.recorder.domain.usecase.gemma

import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.SamplerConfig
import com.roro.core.datastore.Language
import com.roro.recorder.data.GemmaManager
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class SttWithGemmaUseCase @Inject constructor(
    private val gemmaManager: GemmaManager
) {
    companion object {
        private const val CHUNK_SECONDS = 30
    }

    suspend operator fun invoke(file: File, language: Language): String {

        // 타임 스탬프 관련 (2순위)
//        val chunkStartMs = index * CHUNK_SECONDS * 1000L
//        val sentences = result.split(Regex("(?<=[.!?])\\s+"))
//            .filter { it.isNotBlank() }
//
//        sentences.mapIndexed { sentIdx, sentence ->
//            val estimatedMs = chunkStartMs +
//                    (sentIdx.toFloat() / sentences.size * CHUNK_SECONDS * 1000).toLong()
//            "$estimatedMs|$sentence"  // 구분자로 시간 포함
//        }.joinToString("\n")

        val chunks = splitWavToChunks(file, CHUNK_SECONDS)
        val results = mutableListOf<String>()

        chunks.forEachIndexed { index, chunkFile ->
            Timber.d("🎤 청크 ${index + 1}/${chunks.size} STT 중...")
            val result = transcribeChunk(chunkFile, language)
            if (result.isNotBlank()) results.add(result)
            chunkFile.delete()
        }

        return results.joinToString("\n")
    }

    private suspend fun transcribeChunk(chunkFile: File, language: Language): String {
        val langStr = when (language) {
            Language.KOREAN -> "한국어"
            Language.ENGLISH -> "English"
        }

        return try {
            gemmaManager.generateWithAudio(
                audioPath = chunkFile.absolutePath,
                textPrompt = "위 음성을 $langStr 로 그대로 전사해줘. 전사 텍스트만 출력해."
            ).trim()
        } catch (e: Exception) {
            Timber.e(e, "🎤 청크 전사 실패")
            ""
        }
    }

    // 기존 WAV 유틸 그대로
    private fun splitWavToChunks(file: File, chunkSeconds: Int): List<File> {
        val wav = file.readBytes()
        val sampleRate  = wav.getIntLE(24)
        val byteRate    = wav.getIntLE(28)
        val blockAlign  = wav.getShortLE(32)
        val bytesPerChunk = byteRate * chunkSeconds

        val chunks = mutableListOf<File>()
        var offset = 44
        while (offset < wav.size) {
            val end = minOf(offset + bytesPerChunk, wav.size)
            val data = wav.copyOfRange(offset, end)
            val chunkFile = File(file.parent, "chunk_${chunks.size}.wav")
            chunkFile.writeBytes(buildWavHeader(data.size, sampleRate, blockAlign) + data)
            chunks.add(chunkFile)
            offset = end
        }
        return chunks
    }

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
        (this[offset].toInt() and 0xFF) or
                ((this[offset+1].toInt() and 0xFF) shl 8) or
                ((this[offset+2].toInt() and 0xFF) shl 16) or
                ((this[offset+3].toInt() and 0xFF) shl 24)

    private fun ByteArray.getShortLE(offset: Int) =
        ((this[offset].toInt() and 0xFF) or
                ((this[offset+1].toInt() and 0xFF) shl 8)).toShort()

    private fun ByteArray.putIntLE(offset: Int, value: Int) {
        this[offset]   = (value          and 0xFF).toByte()
        this[offset+1] = ((value shr  8) and 0xFF).toByte()
        this[offset+2] = ((value shr 16) and 0xFF).toByte()
        this[offset+3] = ((value shr 24) and 0xFF).toByte()
    }

    private fun ByteArray.putShortLE(offset: Int, value: Int) {
        this[offset]   = (value         and 0xFF).toByte()
        this[offset+1] = ((value shr 8) and 0xFF).toByte()
    }
}