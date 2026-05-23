package com.roro.storage.domain

import com.roro.core.model.FolderWithNoteCount
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 휴지통에서 폴더안에 있는 voiceNote의 개수를 가져오는 유즈케이스
 *
 * @author sehoon
 * @since 2026. 5. 10.
 */
class ObserveTrashFolderItemCountUseCase @Inject constructor(
    private val repository: FileRepository
) {
    operator fun invoke(): Flow<List<FolderWithNoteCount>> {
        return repository.observeTrashFolderItemCount()
    }
}