package com.roro.recorder.domain.usecase.record

import com.roro.recorder.domain.repository.RecordRepository
import java.io.File
import javax.inject.Inject

// 일단 안씀
class TranscribeAudioUseCase @Inject constructor(
    private val recordRepository: RecordRepository
) {

}