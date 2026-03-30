package com.roro.recorder.domain.repository

import java.io.File
import java.util.UUID

//RecordRepository → 순수하게 녹음 기능만 (하드웨어 제어)
// 음성 녹음 (Start, Pause, Resume, Finish)
interface RecordRepository {
    suspend fun processAndSave(audioFile: File, folderId: UUID?): String

}