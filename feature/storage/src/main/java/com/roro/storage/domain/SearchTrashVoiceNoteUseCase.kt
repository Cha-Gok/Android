package com.roro.storage.domain

import com.roro.storage.presentation.search.SearchItem
import javax.inject.Inject

/**
 * 휴지통 > voiceNote 검색 유즈케이스
 *
 * @param 사용자가 검색한 문구
 * @return 폴더 아이템 리스트
 *
 * @author sehoon
 * @since 2026. 5. 15.
 */
class SearchTrashVoiceNoteUseCase @Inject constructor(
    private val repository: FileRepository
) {
    /**
     * 휴지통 내에서 음성 메모를 검색하고, UI 전용 Trash 객체로 변환합니다.
     */
    suspend operator fun invoke(query: String): List<SearchItem.TrashSearchItem> {
        // 1. 검색어가 비어있을 경우 불필요한 DB 접근을 방지합니다.
        if (query.isBlank()) return emptyList()

        // 2. Repository에서 검색된 데이터를 가져와 즉시 SearchItem.Trash로 매핑하여 반환합니다.
        return repository.searchTrashVoiceNotes(query = query).map {
            SearchItem.TrashSearchItem(voiceNoteItem = it)
        }
    }
}
