package com.roro.recorder.domain.usecase.record

import com.roro.recorder.domain.repository.RecordRepository
import javax.inject.Inject

class PauseRecordUseCase @Inject constructor(
    private val recordRepository: RecordRepository
) {
    suspend operator fun invoke() {
        recordRepository.pauseRecording()
    }
}