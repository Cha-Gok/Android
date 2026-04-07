package com.roro.recorder.domain.repository

import java.io.File
import java.util.UUID

//RecordRepository
interface RecordRepository {
    suspend fun saveRecording(
        audioFile: File,
        durationSec: Double,
        sttText: String,
        folderId: UUID? = null
    )

}