package com.roro.recorder.domain.usecase.gemma

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SummaryEligibilityTest {
    @Test
    fun rejects_single_korean_filler() {
        assertFalse(SummaryEligibility.canSummarize("아"))
    }

    @Test
    fun rejects_short_filler_sequence() {
        assertFalse(SummaryEligibility.canSummarize("음 어 네"))
    }

    @Test
    fun rejects_text_shorter_than_minimum_content_length() {
        assertFalse(SummaryEligibility.canSummarize("회의 끝"))
    }

    @Test
    fun accepts_meaningful_transcript() {
        val text = "오늘 회의에서는 다음 주 일정과 담당자 배정에 대해 논의했습니다."

        assertTrue(SummaryEligibility.canSummarize(text))
    }
}
