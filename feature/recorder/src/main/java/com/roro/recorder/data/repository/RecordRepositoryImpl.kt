package com.roro.recorder.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import com.roro.core.dao.VoiceRecordDao
import com.roro.core.mapper.toEntity
import com.roro.core.model.VoiceRecord
import com.roro.recorder.domain.repository.RecordRepository
import com.roro.recorder.domain.repository.VoiceRecordRepository
import java.io.File
import java.io.FileOutputStream

//VoiceRecordRepositoryImpl  →  DB 저장/조회만
//RecordRepositoryImpl       →  AudioRecord 녹음만

class RecordRepositoryImpl(
    private val context: Context
) : RecordRepository {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingThread: Thread? = null
    private var outputFile: File? = null

    @SuppressLint("SupportAnnotationUsage")
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun startRecording() {
        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        outputFile = File(context.filesDir, "recording_${System.currentTimeMillis()}.pcm")

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        )

        audioRecord?.startRecording()
        isRecording = true

        recordingThread = Thread {
            val buffer = ByteArray(bufferSize)
            FileOutputStream(outputFile).use { fos ->
                while (isRecording) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) fos.write(buffer, 0, read)
                }
            }
        }.also { it.start() }
    }

    override fun stopRecording(): File {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        recordingThread?.join()
        return outputFile!!
    }

    override fun isRecording(): Boolean = isRecording
}