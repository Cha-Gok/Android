package com.roro.storage.presentation.trash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.roro.core.navigation.Routes
import com.roro.core.navigation.SearchType
import com.roro.core.ui.component.ChaGokBackground
import com.roro.core.ui.component.ChaGokMenuItem
import com.roro.core.ui.component.ChaGokMoreMenu
import com.roro.core.ui.component.ChaGokTopBarV2
import com.roro.core.ui.component.ChaGokTrashBox
import com.roro.core.ui.component.ChaGokDialog
import com.roro.core.ui.theme.ChaGokTextStyle
import com.roro.core.ui.theme.Danger
import com.roro.core.ui.theme.TextDisabled
import com.roro.core.util.toast
import timber.log.Timber
import java.util.UUID

@Composable
internal fun TrashScreen(
    navController: NavController,
    viewModel: TrashViewModel = hiltViewModel<TrashViewModel>()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    if (uiState.isLoading) {
        Timber.d("로딩 중~")
    }

    uiState.errorMessage?.let {
        Timber.d("text $it")
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is TrashEffect.ShowToast -> context.toast(effect.message)
                TrashEffect.NavigateToDetail -> {

                }

                TrashEffect.NavigateToSearch -> {
                    Timber.d("Navigation to Search")
                    navController.navigate(Routes.searchTemp(SearchType.TRASH))
                }
            }
        }
    }

    TrashScreenContent(
        uiState = uiState,
        // 일반 액션
        onBackClick = {
            if (uiState.isSelectMode) viewModel.onIntent(TrashIntent.ClickCloseSelectMode)
            else navController.popBackStack()
        },
        onSearchClick = { viewModel.onIntent(TrashIntent.ClickSearch) },
        onMoreMenuClick = { viewModel.onIntent(TrashIntent.ClickMore) },
        onDismissMenu = { viewModel.onIntent(TrashIntent.DismissMoreMenu) },

        // 드롭다운 메뉴 아이템 클릭
        onSelectModeClick = { viewModel.onIntent(TrashIntent.ClickSelectMode) },
        onSelectAllItemClick = { viewModel.onIntent(TrashIntent.ClickSelectAllItem) }, // 수정됨
        onEmptyTrashClick = { viewModel.onIntent(TrashIntent.ClickEmptyTrash) }, // 수정됨

        // 선택 모드 액션
        onSelectItem = { id -> viewModel.onIntent(TrashIntent.ClickSelectItem(id)) },
        onRestoreClick = { viewModel.onIntent(TrashIntent.ClickRestoreItems) },
        onRemoveClick = { viewModel.onIntent(TrashIntent.ClickRemoveItems) },

        onDialogConfirm = { viewModel.onIntent(TrashIntent.DialogConfirm) },
        onDialogCancel = { viewModel.onIntent(TrashIntent.DialogCancel) }
    )
}

@Composable
private fun TrashScreenContent(
    uiState: TrashUiState,
    onBackClick: () -> Unit,
    onSearchClick: () -> Unit,
    onMoreMenuClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onSelectModeClick: () -> Unit,
    onSelectAllItemClick: () -> Unit,
    onEmptyTrashClick: () -> Unit,
    onSelectItem: (UUID) -> Unit,
    onRestoreClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onDialogConfirm: () -> Unit,
    onDialogCancel: () -> Unit,
) {
    val menuItems = listOf(
        ChaGokMenuItem(
            text = "전체 선택하기",
            onClick = onSelectAllItemClick
        ),
        ChaGokMenuItem(
            text = "선택하기",
            onClick = onSelectModeClick
        ),
        ChaGokMenuItem(
            text = "휴지통 비우기",
            textColor = Danger,
            onClick = onEmptyTrashClick
        )
    )

    ChaGokBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                ChaGokTopBarV2(
                    title = if (uiState.isSelectMode) "${uiState.selectedIds.size}개 선택됨" else "휴지통",
                    showBackButton = true,
                    backButtonIcon = if (uiState.isSelectMode) Icons.Default.Close else Icons.Default.ArrowBackIosNew,
                    onBackClick = onBackClick,

                    // 1. 일반 모드일 때만 검색 아이콘 노출
                    firstActionIcon = if (!uiState.isSelectMode) Icons.Default.Search else null,
                    firstActionDescription = "검색",
                    onFirstActionClick = onSearchClick,

                    // 2. 일반 모드일 때만 더보기 아이콘 노출
                    secondActionIcon = if (!uiState.isSelectMode) Icons.Default.MoreVert else null,
                    secondActionDescription = "더보기",
                    onSecondActionClick = onMoreMenuClick,

                    // 3. 우측 레이아웃 슬롯 활용
                    secondActionTrailingContent = {
                        if (uiState.isSelectMode) {
                            // 선택 모드일 때: [복원] [삭제] 텍스트 버튼 배치
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                TextButton(onClick = onRestoreClick) {
                                    Text(
                                        text = "복원",
                                        style = ChaGokTextStyle.Body1,
                                        color = Color.White
                                    )
                                }
                                TextButton(onClick = onRemoveClick) {
                                    Text(
                                        text = "삭제",
                                        style = ChaGokTextStyle.Body1,
                                        color = Danger
                                    )
                                }
                            }
                        } else {
                            // 일반 모드일 때: 기존 드롭다운 메뉴
                            ChaGokMoreMenu(
                                expanded = uiState.isMenuExpanded,
                                onDismissRequest = onDismissMenu,
                                items = menuItems
                            )
                        }
                    })
                Text(
                    text = "휴지통에 옮긴 기록은 직접 비우기 전까지 보관되며,\n언제든 복원할 수 있습니다.",
                    style = ChaGokTextStyle.Body1, color = TextDisabled,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.item,
                        key = { it.id } // UUID
                    ) { item ->
                        ChaGokTrashBox(
                            title = item.title,
                            firstText = item.firstText,
                            secondText = item.secondText,
                            type = item.type, // TrashType (FOLDER || VOICE_NOTE)

                            isSelectionMode = uiState.isSelectMode,
                            isSelected = uiState.selectedIds.contains(item.id),

                            onSelectedChange = { isSelected ->
                                onSelectItem(item.id)
                            },
                            onClick = {
                                if (uiState.isSelectMode) {
                                    onSelectItem(item.id)
                                } else {
                                    // 일반모드 인 경우
                                }
                            }
                        )
                    }
                }
            }

            if (uiState.isDialog) {
                // 타입에 따라 제목, 설명, 버튼 텍스트를 결정
                val (title, desc, confirmBtnText) = when (uiState.activeDialogType) {
                    TrashDialogType.REMOVE_SELECTED -> Triple(
                        "선택한 항목을 삭제할까요?",
                        "선택한 항목이 영구 삭제되며\n 되돌릴 수 없어요",
                        "삭제하기"
                    )

                    TrashDialogType.EMPTY_TRASH -> Triple(
                        "휴지통을 비울까요?",
                        "모든 파일이 영구 삭제되며\n 되돌릴 수 없어요",
                        "비우기"
                    )

                    null -> Triple("", "", "")
                }

                ChaGokDialog(
                    title = title,
                    description = desc,
                    onConfirm = onDialogConfirm,
                    onDismiss = onDialogCancel,
                    confirmText = confirmBtnText,
                    buttonColor = Danger,
                    dismissText = "취소"
                )
            }
        }
    }
}


@Composable
@Preview
private fun TrashScreenContentPreview() {
    TrashScreenContent(
        uiState = TrashUiState(

        ),
        onBackClick = { },
        onSearchClick = { },
        onMoreMenuClick = { },
        onSelectAllItemClick = { },
        onDismissMenu = { },
        onSelectModeClick = { },
        onEmptyTrashClick = { },
        onSelectItem = { },
        onRestoreClick = { },
        onRemoveClick = { },
        onDialogConfirm = { },
        onDialogCancel = { },
    )
}
