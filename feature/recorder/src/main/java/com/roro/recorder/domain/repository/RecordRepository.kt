package com.roro.recorder.domain.repository

import android.Manifest
import android.annotation.SuppressLint
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.Flow
import java.io.File

//RecordRepository → 순수하게 녹음 기능만 (하드웨어 제어)
// 음성 녹음 (Start, Pause, Resume, Finish)
interface RecordRepository {

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    suspend fun startRecording(fileName: String)
    suspend fun pauseRecording()
    suspend fun resumeRecording()
    suspend fun finishRecording()

    // observe - 계속 구독, 변화 감지
    fun observeTranscript(): Flow<String>  // STT 텍스트가 바뀔 때마다 emit

    // get - 현재 값 한 번만
    fun getAudioFilePath(): String         // 녹음 끝난 후 파일 경로 한 번만
    fun getDuration(): Double              // 녹음 시간 한 번만

    // stt 변환??
    //fun getTranscriptStream(): Flow<String>
}

