package com.roro.recorder.domain.usecase

import com.roro.recorder.domain.repository.RecordRepository
import java.io.File
import java.util.UUID
import javax.inject.Inject

class SaveRecordingUseCase @Inject constructor(
    private val repository: RecordRepository
) {
    suspend operator fun invoke(
        audioFile: File,
        durationSec: Double,
        sttText: String,
        summaryText: String,
        keywords: List<String>,
        folderId: UUID? = null
    ) {
        repository.saveRecording(
            audioFile = audioFile,
            durationSec = durationSec,
            sttText = sttText,
            summaryText = summaryText,
            keywords = keywords,
            folderId = folderId
        )
    }
}