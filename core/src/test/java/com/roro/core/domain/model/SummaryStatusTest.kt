package com.roro.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SummaryStatusTest {
    @Test
    fun generating_status_exists_for_pending_summary_generation() {
        assertEquals(SummaryStatus.GENERATING, SummaryStatus.valueOf("GENERATING"))
    }

    @Test
    fun insufficient_status_exists_for_too_short_summary_content() {
        assertEquals(SummaryStatus.INSUFFICIENT, SummaryStatus.valueOf("INSUFFICIENT"))
    }
}
