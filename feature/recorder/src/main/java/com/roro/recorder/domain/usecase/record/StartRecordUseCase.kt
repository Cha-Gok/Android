package com.roro.recorder.domain.usecase.record

import android.Manifest
import android.annotation.SuppressLint
import androidx.annotation.RequiresPermission
import com.roro.recorder.domain.repository.RecordRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


// AudioRecord 시작 + ML Kit STT 시작
class StartRecordUseCase @Inject constructor(
    private val recordRepository: RecordRepository
) {
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    suspend operator fun invoke() {
        val fileName = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
            .format(Date(System.currentTimeMillis()))
        recordRepository.startRecording(fileName)
    }
}