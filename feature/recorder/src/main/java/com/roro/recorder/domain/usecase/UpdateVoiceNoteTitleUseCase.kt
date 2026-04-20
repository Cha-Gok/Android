package com.roro.recorder.domain.usecase

import com.roro.core.dao.VoiceNoteDao
import java.util.UUID
import javax.inject.Inject

/**
 * 기능 설명:
 * - VoiceNote 제목을 수정하는 UseCase
 * - 공백 입력 시 이전 제목 유지
 *
 * @author
 * @since 2026. 04. 19.
 */
class UpdateVoiceNoteTitleUseCase @Inject constructor(
    private val voiceNoteDao: VoiceNoteDao
) {
    suspend operator fun invoke(
        voiceNoteId: UUID,
        newTitle: String,
        currentTitle: String
    ): String {
        val trimmed = newTitle.trim()
        val finalTitle = if (trimmed.isBlank()) currentTitle else trimmed  // 공백이면 이전 이름 유지

        if (finalTitle != currentTitle) {
            voiceNoteDao.renameVoiceNote(
                noteId = voiceNoteId,
                voiceNoteTitle = finalTitle,
                updatedAt = System.currentTimeMillis()
            )
        }

        return finalTitle
    }
}