package com.roro.recorder.domain.usecase.gemma

import com.roro.core.domain.model.SummaryStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class RecordingTranscriptPolicyTest {
    @Test
    fun initial_save_marks_meaningful_transcript_as_generating() {
        val transcript = "오늘 회의에서는 다음 주 일정과 담당자 배정에 대해 논의했습니다."

        val status = RecordingTranscriptPolicy.initialSummaryStatus(transcript)

        assertEquals(SummaryStatus.GENERATING, status)
    }

    @Test
    fun initial_save_marks_short_transcript_as_insufficient() {
        val transcript = "네"

        val status = RecordingTranscriptPolicy.initialSummaryStatus(transcript)

        assertEquals(SummaryStatus.INSUFFICIENT, status)
    }
}
