package com.roro.storage.presentation.trash

import com.roro.core.domain.model.TrashItem
import java.util.UUID

data class TrashUiState(
    val isLoading: Boolean = false,
    val item: List<TrashItem> = emptyList(), // 휴지통 전체 리스트
    val isSelectMode: Boolean = false,       // 선택 모드 여부
    val selectedIds: Set<UUID> = emptySet(), // 선택된 아이템 ID들
    val isMenuExpanded: Boolean = false,     // 더보기 메뉴 확장 여부
    val activeDialogType: TrashDialogType? = null, // null이면 다이얼로그 안 띄움
    val isDialog: Boolean = false,
    val errorMessage: String? = null
)

enum class TrashDialogType{
    REMOVE_SELECTED,    // 선택 항목 삭제
    EMPTY_TRASH         // 휴지통 비우기
}