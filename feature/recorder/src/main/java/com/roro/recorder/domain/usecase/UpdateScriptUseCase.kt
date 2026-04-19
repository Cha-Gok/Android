package com.roro.recorder.domain.usecase

import com.roro.core.dao.TranscriptDao
import com.roro.core.entity.TranscriptEntity
import java.util.UUID
import javax.inject.Inject

/**
 * 기능 설명:
 * - 스크립트(STT 텍스트)를 수정하고 DB에 업데이트하는 UseCase
 *
 * @author hyeonseo
 * @since 2026. 04. 19.
 */
class UpdateScriptUseCase @Inject constructor(
    private val transcriptDao: TranscriptDao
) {
    suspend operator fun invoke(
        voiceNoteId: UUID,
        newText: String
    ) {
        transcriptDao.upsert(
            TranscriptEntity(
                id = UUID.randomUUID(),
                voiceNoteId = voiceNoteId,
                text = newText,
                createdAt = System.currentTimeMillis()
            )
        )
    }
}
