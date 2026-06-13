package com.roro.storage.presentation.folderlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.roro.core.domain.model.FolderItem
import com.roro.core.navigation.Routes
import com.roro.core.navigation.SearchType
import com.roro.core.ui.component.ChaGokBackground
import com.roro.core.ui.component.ChaGokDialogCreateFolder
import com.roro.core.ui.component.ChaGokSwipeableFolderItem
import com.roro.core.ui.component.ChaGokTopBarV2
import com.roro.core.util.toast
import com.roro.storage.presentation.home.FolderDialogType
import java.util.UUID

@Composable
fun PrivateFolderScreen(
    navController: NavController,
    viewModel: PrivateFolderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onIntent(PrivateFolderIntent.Initialize)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                PrivateFolderEffect.NavigateBack -> navController.popBackStack()
                is PrivateFolderEffect.NavigateFileList -> {
                    navController.navigate(Routes.storageFile(effect.folderId, effect.folderName))
                }

                PrivateFolderEffect.NavigateSearch -> {
                    navController.navigate(
                        Routes.searchTemp(
                            searchType = SearchType.FOLDER,
                        )
                    )
                }

                is PrivateFolderEffect.ShowToast -> context.toast(effect.message)
            }
        }
    }


    PrivateFolderScreenContent(
        uiState = uiState,
        onIntent = viewModel::onIntent
    )
}

@Composable
internal fun PrivateFolderScreenContent(
    uiState: PrivateFolderUiState,
    onIntent: (PrivateFolderIntent) -> Unit
) {
    ChaGokBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                ChaGokTopBarV2(
                    title = "개인폴더",
                    showBackButton = true,
                    onBackClick = { onIntent(PrivateFolderIntent.ClickBack) },
                    firstActionIcon = Icons.Outlined.Search,
                    onFirstActionClick = { onIntent(PrivateFolderIntent.ClickSearch) },
                    secondActionIcon = Icons.Outlined.CreateNewFolder,
                    onSecondActionClick = {
                        onIntent(PrivateFolderIntent.ShowCreateFolderDialog(true))
                    },
                )
                PrivateFolderList(
                    uiState = uiState,
                    onIntent = onIntent,
                    folders = uiState.folderList,
                    // 리스트에서 수정 버튼 클릭 시 실행될 로직
                    onEditFolder = { folder ->
                        onIntent(PrivateFolderIntent.ShowModifyFolderDialog(folder))
                    },
                    onDeleteFolder = { folder ->
                        onIntent(PrivateFolderIntent.RemoveFolder(folder))
                    },
                    onFolderClick = { folder ->
                        onIntent(PrivateFolderIntent.ClickPrivateFolder(folder))
                    }
                )
            }
        }

        // 다이얼로그 처리 로직
        uiState.dialogType?.let { type ->
            val isRename = type == FolderDialogType.RENAME

            ChaGokDialogCreateFolder(
                title = if (isRename) "폴더 이름 수정" else "새 폴더",
                description = if (isRename) "수정할 폴더의 이름을\n입력해주세요." else "새로 만들 폴더의 이름을\n입력해주세요.",
                dismissText = "취소",
                confirmText = if (isRename) "수정하기" else "만들기",
                inputText = uiState.inputFolderName,
                onValueChange = { onIntent(PrivateFolderIntent.InputFolderName(it)) },
                onDismiss = { onIntent(PrivateFolderIntent.DismissDialog) },
                onConfirm = { onIntent(PrivateFolderIntent.ConfirmDialog) },
            )
        }
    }
}

@Composable
fun PrivateFolderList(
    uiState: PrivateFolderUiState,
    onIntent: (PrivateFolderIntent) -> Unit,
    folders: List<FolderItem>,
    onEditFolder: (FolderItem) -> Unit,
    onDeleteFolder: (FolderItem) -> Unit,
    onFolderClick: (FolderItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 14.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 2. 리스트 아이템 구성
        items(items = folders, key = { it.id }) { folder ->
            // 여기에 이미 만들어둔 공통 컴포넌트를 호출합니다.
            val count = folder.count
            ChaGokSwipeableFolderItem(
                id = folder.id,
                text = folder.title,
                count = count,
                isRevealed = uiState.swipeFolder?.id == folder.id,
                onExpand = { onIntent(PrivateFolderIntent.OnFolderSwipe(folder)) },
                onCollapse = {
                    if (uiState.swipeFolder?.id == folder.id) {
                        onIntent(PrivateFolderIntent.OnFolderSwipe(null))
                    }
                },
                onEdit = { onEditFolder(folder) },
                onDelete = { onDeleteFolder(folder) },
                onClick = { onFolderClick(folder) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PrivateFolderScreenPreview() {
    // 1. Preview에서 보여줄 가상의 데이터 생성
    val mockFolders = listOf(
        FolderItem(id = UUID.randomUUID().toString(), title = "여행 기록", count = "5", createAt = 1776769324521L),
        FolderItem(id = UUID.randomUUID().toString(), title = "업무 미팅", count = "12", createAt = 1776769342203L),
        FolderItem(id = UUID.randomUUID().toString(), title = "아이디어함", count = "0", createAt = 1776771640900L)
    )

    // 2. 새로운 UiState 타입으로 교체
    PrivateFolderScreenContent(
        uiState = PrivateFolderUiState(
            folderList = mockFolders,
            isLoading = false,
            inputFolderName = ""
        ),
        // 3. onIntent는 함수이므로 빈 람다 전달
        onIntent = {}
    )
}