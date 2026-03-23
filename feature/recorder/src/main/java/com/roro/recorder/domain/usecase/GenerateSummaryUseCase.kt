package com.roro.recorder.domain.usecase

import com.roro.recorder.domain.repository.VoiceNoteRepository

// 요약 생성
// 키워드 추출도 여기서(?) - ExtractKeyword
class GenerateSummaryUseCase(
    private val repository: VoiceNoteRepository
) {
    suspend operator fun invoke(noteId: String): String {

        // 1. transcript 가져오기
        // 2. 요약 생성 (AI or 로직)
        // 3. 저장

        val summary = "요약 결과"

        //repository.updateSummary(noteId, summary)

        return summary
    }
}