package com.roro.recorder.domain.usecase.gemma

object ProofreadLineValidator {
    fun choose(original: String, proofread: String): String {
        val originalLines = original.lines().filter { it.isNotBlank() }.size
        val proofreadLines = proofread.lines().filter { it.isNotBlank() }.size
        return if (originalLines == proofreadLines) proofread else original
    }
}
