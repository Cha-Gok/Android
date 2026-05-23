package com.roro.storage.presentation.folderlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.roro.core.model.Folder
import com.roro.core.navigation.Routes
import com.roro.core.navigation.SearchType
import com.roro.core.ui.component.ChaGokBackground
import com.roro.core.ui.component.ChaGokDialogCreateFolder
import com.roro.core.ui.component.ChaGokSwipeableFolderItem
import com.roro.core.ui.component.ChaGokTopBar2
import com.roro.core.ui.component.ChaGokTopBarV2
import com.roro.core.ui.component.TopBarIcon
import com.roro.core.util.toast
import com.roro.storage.presentation.StorageEffect
import com.roro.storage.presentation.StorageIntent
import com.roro.storage.presentation.home.DefaultFolderType
import com.roro.storage.presentation.home.FolderDialogType
import com.roro.storage.presentation.home.StorageUiState
import com.roro.storage.presentation.home.StorageViewModel
import timber.log.Timber
import java.util.UUID

@Composable
fun PrivateFolderScreen(
    navController: NavController, viewModel: StorageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val folders by viewModel.userFolders.collectAsState()
    val folderCount by viewModel.folderItemCountMap.collectAsState()
    val context = LocalContext.current
    var folderName by rememberSaveable { mutableStateOf("") }

    if (uiState.isLoading) {
        Timber.d("로딩 중~")
    }

    uiState.errorMessage?.let {
        Timber.d("text $it")
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is StorageEffect.ClearFolderInput -> {
                    folderName = ""
                }

                is StorageEffect.ShowToast -> context.toast(effect.message)
            }
        }
    }


    PrivateFolderScreenContent(
        uiState = uiState,
        navController = navController,
        folders = folders,
        folderName = folderName,
        folderCount = folderCount,
        onFolderNameChange = { folderName = it },
        onCreateFolder = { viewModel.onIntent(StorageIntent.CreateFolder(it)) },
        onDeleteFolder = { folder -> viewModel.onIntent(StorageIntent.MoveToTrash(folder = folder)) },
        onRenameFolder = { folder -> viewModel.onIntent(StorageIntent.RenameFolder(folder = folder)) }
    )
}

@Composable
internal fun PrivateFolderScreenContent(
    uiState: StorageUiState,
    navController: NavController,
    folders: List<Folder>,
    folderName: String,
    folderCount: Map<UUID?, Int>,
    onFolderNameChange: (String) -> Unit,
    onCreateFolder: (String) -> Unit,
    onDeleteFolder: (Folder) -> Unit,
    onRenameFolder: (Folder) -> Unit,
) {
    // 다이얼로그 타입과 선택된 폴더를 관리하는 상태
    var dialogType by remember { mutableStateOf<FolderDialogType?>(null) }
    var selectedFolder by remember { mutableStateOf<Folder?>(null) }

    ChaGokBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                ChaGokTopBarV2(
                    title = "개인폴더",
                    showBackButton = true,
                    onBackClick = { navController.popBackStack() },
                    firstActionIcon = Icons.Outlined.Search,
                    onFirstActionClick = { navController.navigate(Routes.searchTemp(SearchType.FOLDER)) },
                    secondActionIcon = Icons.Outlined.CreateNewFolder,
                    onSecondActionClick = {
                        onFolderNameChange("")
                        dialogType = FolderDialogType.CREATE
                    },
                )
//                ChaGokTopBar2(
//                    title = "개인폴더",
//                    onBackClick = { navController.popBackStack() },
//                    showBackButton = true,
//                    actions = {
//                        TopBarIcon(
//                            imageVector = Icons.Outlined.CreateNewFolder,
//                            onClick = {
//                                onFolderNameChange("") // 이름 초기화
//                                dialogType = FolderDialogType.CREATE
//                            }
//                        )
//                    }
//                )
                PrivateFolderList(
                    navController = navController,
                    folders = folders,
                    folderCount = folderCount,
                    // 리스트에서 수정 버튼 클릭 시 실행될 로직
                    onEditFolder = { folder ->
                        selectedFolder = folder
                        onFolderNameChange(folder.name) // 기존 이름을 입력창에 채움
                        dialogType = FolderDialogType.RENAME // 다이얼로그 띄움
                    },
                    onDeleteFolder = onDeleteFolder,
                )
            }
        }

        // 다이얼로그 처리 로직
        dialogType?.let { type ->
            val isRename = type == FolderDialogType.RENAME

            ChaGokDialogCreateFolder(
                title = if (isRename) "폴더 이름 수정" else "새 폴더",
                description = if (isRename) "수정할 폴더의 이름을\n입력해주세요." else "새로 만들 폴더의 이름을\n입력해주세요.",
                dismissText = "취소",
                confirmText = if (isRename) "수정하기" else "만들기",
                inputText = folderName,
                onValueChange = onFolderNameChange,
                onDismiss = {
                    dialogType = null
                    selectedFolder = null
                    onFolderNameChange("")
                },
                onConfirm = {
                    if (folderName.isNotBlank()) {
                        if (isRename && selectedFolder != null) {
                            // 다이얼로그에서 수정 버튼을 눌렀을 때 실행
                            onRenameFolder(selectedFolder!!.copy(name = folderName))
                        } else {
                            // 다이얼로그에서 만들기 버튼을 눌렀을 때 실행
                            onCreateFolder(folderName)
                        }
                        dialogType = null
                        selectedFolder = null
                    }
                }
            )
        }
    }
}

@Composable
fun PrivateFolderList(
    navController: NavController,
    folders: List<Folder>,
    folderCount: Map<UUID?, Int>,
    onEditFolder: (Folder) -> Unit,
    onDeleteFolder: (Folder) -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. 상태 관리: 어떤 아이템이 열려 있는지 (ID로 관리)
    var revealedFolderId by remember { mutableStateOf<UUID?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 14.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 2. 리스트 아이템 구성
        items(items = folders, key = { it.id }) { folder ->
            // 여기에 이미 만들어둔 공통 컴포넌트를 호출합니다.
            val count = folderCount[folder.id] ?: 0
            ChaGokSwipeableFolderItem(
                id = folder.id,
                text = folder.name,
                count = "$count", // 필요시 실제 데이터 연결
                isRevealed = revealedFolderId == folder.id,
                onExpand = { revealedFolderId = folder.id },
                onCollapse = { if (revealedFolderId == folder.id) revealedFolderId = null },
                onEdit = { onEditFolder(folder) },
                onDelete = { onDeleteFolder(folder) },
                onClick = {
                    Timber.d("이동 버튼ㄴ 클릭")
                    navController.navigate(Routes.storageFile(folderId = folder.id.toString(), folderName = folder.name))
                }

            )
        }
    }
}

@Preview
@Composable
fun PrivateFolderScreenPreview() {
    PrivateFolderScreenContent(
        navController = rememberNavController(),
        uiState = StorageUiState(
            selectedFolderType = DefaultFolderType.RECENT, isLoading = false, errorMessage = null
        ),
        folderName = "TODO()",
        onFolderNameChange = {},
        onCreateFolder = { },
        folders = emptyList(),
        onDeleteFolder = { TODO() },
        onRenameFolder = { TODO() },
        folderCount = emptyMap(),
    )
}
