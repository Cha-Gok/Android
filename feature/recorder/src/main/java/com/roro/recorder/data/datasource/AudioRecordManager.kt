package com.roro.recorder.data.datasource

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import java.io.File
import java.io.FileOutputStream

class AudioRecordManager {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false

    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        channelConfig,
        audioFormat
    )

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecording(
        outputFile: File,
        onAudioData: (ByteArray) -> Unit
    ) {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        val outputStream = FileOutputStream(outputFile)

        // 🔥 WAV 헤더 먼저 작성
        writeWavHeader(outputStream, sampleRate, 1, 16)

        isRecording = true
        audioRecord?.startRecording()

        Thread {
            val buffer = ByteArray(bufferSize)

            while (isRecording) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0

                if (read > 0) {
                    // 1️⃣ 파일 저장
                    outputStream.write(buffer, 0, read)

                    // 2️⃣ STT 전달 (복사해서 넘김🔥)
                    val copy = buffer.copyOf(read)
                    onAudioData(copy)
                }
            }

            outputStream.close()
        }.start()
    }

    fun stopRecording(file: File) {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        // 🔥 WAV 헤더 수정
        updateWavHeader(file)
    }
}