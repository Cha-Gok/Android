package com.roro.recorder.domain.repository

import com.roro.core.domain.model.FolderItem
import com.roro.core.domain.model.SummaryStatus
import com.roro.core.model.Keyword
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.UUID

/**
 * 기능 설명:
 * - 녹음 데이터 저장을 위한 Repository 인터페이스
 *
 * @author hyeonseo
 * @since 2026. 04. 12.
 */
interface RecordRepository {
    suspend fun saveRecording(
        audioFile: File,
        durationSec: Double,
        sttText: String,
        summaryText: String,
        keywords: List<String>,
        folderId: UUID? = null,

        summaryStatus: SummaryStatus = SummaryStatus.NONE
    ): UUID  // Unit → UUID

    suspend fun createUserFolder(
        folderName: String
    ): Boolean

    suspend fun moveToFolder(voiceNoteId: List<UUID>, folderId: String)

    // voiceNote 휴지통으로 이동
    suspend fun moveToVoiceNotes(voiceNoteIds: List<UUID>)

    /*      폴더 정보 가져오기      */
    fun observeFolders(): Flow<List<FolderItem>>
}