package com.roro.storage.domain

import com.roro.storage.presentation.search.SearchItem
import javax.inject.Inject

/**
 * 홈 > 폴더 검색 유즈케이스
 *
 * @param  사용자가 검색한 문구
 * @return  폴더 아이템 리스트
 *
 * @author sehoon
 * @since 2026. 5. 15.
 */
class SearchFolderUseCase @Inject constructor(
    private val repository: FileRepository
) {
    suspend operator fun invoke(query: String): List<SearchItem.TrashSearchItem> {
        if (query.isBlank()) return emptyList()

        // 변수 할당 없이 바로 map 연산 후 반환
        return repository.searchFolders(query = query).map {
            SearchItem.TrashSearchItem(folderItem = it)
        }
    }
}