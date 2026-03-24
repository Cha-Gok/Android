package com.roro.recorder.domain.usecase

import com.roro.core.model.VoiceNote
import javax.inject.Inject
import com.roro.recorder.domain.repository.VoiceNoteRepository


// 녹음 완료 후 저장
//class SaveVoiceRecordUseCase @Inject constructor(
//    private val voiceNoteRepository: VoiceNoteRepository
//) {
//    suspend operator fun invoke(
//        title: String,
//        audioFilePath: String,
//        durationSec: Double   // 👈 여기 추가
//    ): VoiceNote {
//        return voiceNoteRepository.createVoiceAll(
//            title = title,
//            audioFilePath = audioFilePath,
//            durationSec = durationSec   // 👈 이렇게 넘겨야 함
//        )
//    }
//}