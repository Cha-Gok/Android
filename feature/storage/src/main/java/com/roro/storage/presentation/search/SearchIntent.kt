package com.roro.storage.presentation.search

sealed interface SearchIntent {
    data class InitSearchType(val searchType: String, val folderId: String?) : SearchIntent
    data class ChangeQuery(val query: String) : SearchIntent
    data object ClickKeyboardSearch : SearchIntent
    data object ClickClose : SearchIntent
    data class ClickItem(val item: Any) : SearchIntent
}


sealed interface SearchEffect {
    // 1. 화면 뒤로가기 실행 (실제 네비게이션 동작)
    data object NavigateBack : SearchEffect

    // 2. 상세 화면으로 이동
    data class NavigateToDetail(val id: String) : SearchEffect

    // 3. 검색 결과 없음 등 알림
    data class ShowToast(val message: String) : SearchEffect
}