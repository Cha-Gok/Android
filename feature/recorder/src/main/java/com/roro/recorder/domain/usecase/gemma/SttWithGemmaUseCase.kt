package com.roro.recorder.domain.usecase.gemma

import com.roro.core.datastore.Language
import com.roro.core.gemma.GemmaManager
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
        val chunks = splitWavToChunks(file, CHUNK_SECONDS)
        val results = mutableListOf<String>()

        chunks.forEachIndexed { index, chunkFile ->
            Timber.d("🎤 청크 ${index + 1}/${chunks.size} STT 중...")
            if (!isChunkSilent(chunkFile)) {
                val result = transcribeChunk(chunkFile, language)
                if (result.isNotBlank()) results.add(result)
            } else {
                Timber.d("🔇 청크 ${index + 1} 무음 → 건너뜀")
            }
            chunkFile.delete()
        }

        return results.joinToString("\n")
    }

    private suspend fun transcribeChunk(chunkFile: File, language: Language): String {
        val langStr = when (language) {
            Language.KOREAN -> "Korean"
            Language.ENGLISH -> "English"
        }

        return try {
            gemmaManager.generateWithAudio(
                audioPath = chunkFile.absolutePath,
                textPrompt = "Transcribe the audio above in $langStr exactly as spoken. Output only the transcribed text. If there is no speech, output nothing."
            ).trim()
        } catch (e: Exception) {
            Timber.e(e, "🎤 청크 전사 실패")
            ""
        }
    }


    private fun isChunkSilent(chunkFile: File): Boolean {
        val wav = chunkFile.readBytes()
        if (wav.size <= 44) return true

        val pcm = wav.drop(44).toByteArray()
        var sum = 0.0
        var count = 0
        var i = 0
        while (i + 1 < pcm.size) {
            val sample = ((pcm[i].toInt() and 0xFF) or (pcm[i+1].toInt() shl 8)).toShort()
            sum += sample * sample
            count++
            i += 2
        }
        val rms = if (count > 0) Math.sqrt(sum / count) else 0.0
        return rms < 300.0  // 임계값, 조정 가능
    }


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