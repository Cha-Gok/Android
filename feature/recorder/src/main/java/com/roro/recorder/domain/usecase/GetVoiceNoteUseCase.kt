package com.roro.recorder.domain.usecase

import com.roro.core.dao.VoiceNoteDao
import com.roro.core.dao.TranscriptDao
import com.roro.core.dao.SummaryDao
import com.roro.core.dao.KeywordDao
import java.util.UUID
import javax.inject.Inject

data class VoiceNoteResult(
    val title: String,
    val sttText: String,
    val summaryText: String,
    val keywords: List<String>
)

class GetVoiceNoteUseCase @Inject constructor(
    private val voiceNoteDao: VoiceNoteDao,
    private val transcriptDao: TranscriptDao,
    private val summaryDao: SummaryDao,
    private val keywordDao: KeywordDao,
) {
    suspend operator fun invoke(voiceNoteId: UUID): VoiceNoteResult? {
        val voiceNote = voiceNoteDao.getNote(voiceNoteId) ?: return null
        val transcript = transcriptDao.getByVoiceNoteId(voiceNoteId)
        val summary = summaryDao.getByVoiceNoteId(voiceNoteId)
        val keywords = keywordDao.getByVoiceNoteId(voiceNoteId)

        return VoiceNoteResult(
            title = voiceNote.title,
            sttText = transcript?.text.orEmpty(),
            summaryText = summary?.text.orEmpty(),
            keywords = keywords.map { it.word }
        )
    }
}