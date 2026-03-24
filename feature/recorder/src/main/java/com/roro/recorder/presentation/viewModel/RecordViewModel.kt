package com.roro.recorder.presentation.viewModel

import android.Manifest
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roro.recorder.domain.repository.RecordRepository
import com.roro.recorder.domain.usecase.SaveTranscriptUseCase
import com.roro.recorder.domain.usecase.SaveVoiceRecordUseCase
import com.roro.recorder.domain.usecase.record.TranscribeAudioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val recordRepository: RecordRepository,
    private val saveVoiceRecordUseCase: SaveVoiceRecordUseCase,
    private val saveTranscriptUseCase: SaveTranscriptUseCase,
    private val transcribeAudioUseCase : TranscribeAudioUseCase,
    // private val speechManager: SpeechManager
) : ViewModel() {

    // 🎙 녹음 상태
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    // 🧠 STT 텍스트 (UI용)
    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText

    // 내부 저장용
    private var finalText: String = ""
    private var startTime: Long = 0

//    init {
//        speechManager.onFinalResult = { text ->
//            finalText = text
//            _recognizedText.value = text
//        }
//
//        speechManager.onPartialResult = { text ->
//            _recognizedText.value = text   // 🔥 실시간 반영
//        }
//    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
//    fun startRecording() {
//        startTime = System.currentTimeMillis()
//
//        finalText = "" // 초기화
//        _recognizedText.value = ""
//
//        recordRepository.startRecording("새녹음")
//        speechManager.startListening()
//
//        _isRecording.value = true
//        _isPaused.value = false
//    }
    fun startRecording() {
        startTime = System.currentTimeMillis()

        finalText = ""
        _recognizedText.value = ""

        //speechManager.startListening()   // 🔥 먼저 시작

        Handler(Looper.getMainLooper()).postDelayed({
            recordRepository.startRecording("새녹음")  // 🔥 0.5초 뒤 시작
        }, 500)

        _isRecording.value = true
    }

    fun pauseRecording() {
        recordRepository.pauseRecording()
        _isPaused.value = true
    }

    fun resumeRecording() {
        recordRepository.resumeRecording()
        _isPaused.value = false
    }

//    fun stopRecording() {
//        val file = recordRepository.stopRecording()
//
//        val duration = (System.currentTimeMillis() - startTime) / 1000.0
//
//        _isRecording.value = false
//        _isPaused.value = false
//
//        viewModelScope.launch {
//
//            // 1️⃣ 음성 파일 저장
//            val note = saveVoiceRecordUseCase(
//                title = "새녹음",
//                audioFilePath = file.absolutePath,
//                durationSec = duration
//            )
//
//            // 2️⃣ 텍스트 저장 🔥 (핵심)
//            saveTranscriptUseCase(
//                voiceNoteId = note.id,
//                text = finalText
//            )
//
//            Timber.d("duration: $duration")
//            Timber.d("text: $finalText")
//        }
//    }

    fun stopRecording() {
        val file = recordRepository.stopRecording()

        val duration = (System.currentTimeMillis() - startTime) / 1000.0

        _isRecording.value = false
        _isPaused.value = false

        viewModelScope.launch {

            // 1️⃣ 음성 파일 저장
            val note = saveVoiceRecordUseCase(
                title = "새녹음",
                audioFilePath = file.absolutePath,
                durationSec = duration
            )

            // 2️⃣ STT 변환 🔥
            val text = transcribeAudioUseCase(file)

            // 3️⃣ 텍스트 저장
            saveTranscriptUseCase(
                voiceNoteId = note.id,
                text = text
            )

            Timber.d("duration: $duration")
            Timber.d("text: $text")
        }
    }
}