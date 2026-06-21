package com.roro.recorder.domain.usecase.gemma

import org.junit.Assert.assertEquals
import org.junit.Test

class ProofreadLineValidatorTest {
    @Test
    fun returns_proofread_text_when_line_count_matches() {
        val original = "a\nb"
        val proofread = "A\nB"

        assertEquals(proofread, ProofreadLineValidator.choose(original, proofread))
    }

    @Test
    fun returns_original_when_line_count_changes() {
        val original = "a\nb"
        val proofread = "A B"

        assertEquals(original, ProofreadLineValidator.choose(original, proofread))
    }
}
