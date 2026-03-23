package com.roro.recorder.domain.repository

import java.io.File

interface RecordRepository {
    fun startRecording()
    fun stopRecording(): File
    fun isRecording(): Boolean
}