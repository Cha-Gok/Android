package com.roro.storage.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.roro.core.model.Folder
import com.roro.core.model.VoiceNote
import com.roro.core.ui.component.ChaGokBackground
import com.roro.core.ui.component.ChaGokDialogCreateFolder
import com.roro.core.ui.component.ChaGokFolderBox
import com.roro.core.ui.component.ChaGokSwipeableFileItem
import com.roro.core.ui.component.ChaGokTopBar2
import com.roro.core.ui.component.SortType
import com.roro.core.ui.component.SummaryStatus
import com.roro.core.ui.component.TopBarIcon
import com.roro.core.ui.component.TopBarMoreMenu
import com.roro.core.ui.theme.ChaGokTextStyle
import com.roro.core.ui.theme.Danger
import com.roro.core.ui.theme.Gray100
import com.roro.core.ui.theme.TextPrimary
import com.roro.core.util.formatTime
import com.roro.core.util.toast
import com.roro.storage.presentation.home.DefaultFolderType
import com.roro.storage.presentation.home.StorageUiState
import com.roro.storage.presentation.home.StorageViewModel
import timber.log.Timber
import java.util.UUID

@Composable
fun FileListScreen(
    navController: NavController,
    folderName: String,
    folderId: String,
    viewModel: StorageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val folders by viewModel.userFolders.collectAsState()
    val context = LocalContext.current

    val voiceNotes by viewModel.voiceNoteFolderList.collectAsState()
    var fileName by rememberSaveable { mutableStateOf("") }

    // 1. 전달 받은 folderId를 변환
    val uuid = remember(folderId) {
        runCatching { UUID.fromString(folderId) }.getOrNull()
    }

    LaunchedEffect(uuid) {
        uuid?.let {
            Timber.d("폴더 데이터 로드 시작: $it")
            viewModel.onIntent(StorageIntent.FetchVoiceNote(it))
        }
    }


    Timber.d("FileListScreen folder = $folderId")
    Timber.d("FileListScreen folderName = $folderName")

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
                    fileName = ""
                }

                is StorageEffect.ShowToast -> context.toast(effect.message)
            }
        }
    }


    FileListScreenContent(
        uiState = uiState,
        navController = navController,
        voiceNotes = voiceNotes,
        folderName = folderName,
        folders = folders,
        onDeleteVoiceNote = { viewModel.onIntent(StorageIntent.MoveToTrashVoiceNotes(listOf(it.id))) },
        onSortByCreate = { viewModel.onIntent(StorageIntent.SortByCreatedAt) },
        onSortByUpdate = { viewModel.onIntent(StorageIntent.SortByUpdatedAt) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FileListScreenContent(
    uiState: StorageUiState,
    navController: NavController,
    voiceNotes: List<VoiceNote>,
    folderName: String,
    folders: List<Folder>,
    onSortByCreate: () -> Unit,
    onSortByUpdate: () -> Unit,
    onDeleteVoiceNote: (VoiceNote) -> Unit,
) {
    // 다이얼로그 타입과 선택된 폴더를 관리하는 상태
    var isEditMode by remember { mutableStateOf(false) }
    // enum 대신 Boolean으로 삭제 다이얼로그 상태만 관리
    var showDeleteDialog by remember { mutableStateOf(false) }
// 선택된 ID들을 관리하는 상태
    var selectedIds by remember { mutableStateOf(setOf<UUID>()) }
    // 현재 정렬 상태 관리 (기본값: 생성일순)
    var currentSortType by rememberSaveable { mutableStateOf(SortType.CREATED_AT) }
    // 1. 바텀시트 표시 상태 추가
    var showMoveBottomSheet by remember { mutableStateOf(false) }
    BackHandler(enabled = isEditMode) {
        isEditMode = false
        selectedIds = emptySet()
    }
    ChaGokBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                ChaGokTopBar2(
                    title = if (isEditMode) "${selectedIds.size}개" else folderName,
                    backIcon = if (isEditMode) Icons.Default.Close else Icons.Default.ArrowBackIosNew,
                    onBackClick = {
                        if (isEditMode) {
                            isEditMode = false
                            selectedIds = emptySet() // 선택 해제
                        } else navController.popBackStack()
                    },
                    showBackButton = true,
                    actions = {
                        if (isEditMode) {
                            // 2. 편집 모드일 때: 이동, 삭제 텍스트 버튼
                            TextButton(
                                onClick = {
                                    showMoveBottomSheet = true
                                }
                            ) {
                                Text(text = "이동", color = TextPrimary, style = ChaGokTextStyle.Title2)
                            }

                            TextButton(
                                onClick = {
                                    Timber.d("선택된 파일들 삭제: $selectedIds")
                                    // onDeleteVoiceNote를 리스트를 받게 수정하거나 반복문 처리
                                    if (selectedIds.isNotEmpty()) {
                                        showDeleteDialog = true
                                    }
                                }
                            ) {
                                Text(text = "삭제", color = Danger, style = ChaGokTextStyle.Title2)
                            }
                        } else {
                            TopBarIcon(
                                imageVector = Icons.Outlined.Search,
                                onClick = {
                                    Timber.d("검색창 이동")
                                }
                            )
                            TopBarMoreMenu(
                                currentSortType = currentSortType,
                                onSortByCreate = {
                                    currentSortType = SortType.CREATED_AT
                                    onSortByCreate()
                                },
                                onSortByUpdate = {
                                    currentSortType = SortType.UPDATED_AT
                                    onSortByUpdate()
                                },
                                onSelectAll = {
                                    if (selectedIds.size == voiceNotes.size) {
                                        selectedIds = emptySet()
                                    } else {
                                        selectedIds = voiceNotes.map { it.id }.toSet()
                                        isEditMode = true
                                    }
                                },
                                onSelectMode = { isEditMode = true }
                            )
                        }
                    }
                )
                FileVoiceNoteList(
                    voiceNotes = voiceNotes,
                    // 리스트에서 수정 버튼 클릭 시 실행될 로직
                    onEditFolder = { folder ->
//                        selectedFolder = folder
//                        onFolderNameChange(folder.name) // 기존 이름을 입력창에 채움
//                        dialogType = FolderDialogType.RENAME // 다이얼로그 띄움
                    },
                    onDeleteFolder = onDeleteVoiceNote,
                    isSelectionMode = isEditMode,
                    selectedIds = selectedIds,
                    onSelectionChange = { newSet ->
                        selectedIds = newSet
                    },
                )
            }
        }

        if (showDeleteDialog) {
            ChaGokDialogCreateFolder(
                title = "기록을 삭제할까요",
                description = "휴지통으로 이동되며, 직접\n비우기 전까지 보관돼요",
                dismissText = "취소",
                confirmText = "삭제",
                inputText = "",
                onValueChange = {},
                onDismiss = {
                    showDeleteDialog = false
                },
                onConfirm = {
                    voiceNotes.filter { it.id in selectedIds }.forEach {
                        onDeleteVoiceNote(it)
                    }
                    showDeleteDialog = false
                    isEditMode = false
                    selectedIds = emptySet()
                }
            )
        }

        // FileListScreenContent.kt 하단
        if (showMoveBottomSheet) {
            val sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            )

            ModalBottomSheet(
                onDismissRequest = { showMoveBottomSheet = false },
                sheetState = rememberModalBottomSheetState(),
                containerColor = Gray100
            ) {
                // 바텀시트 내부 콘텐츠
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, start = 20.dp, end = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween, // 양끝 정렬
                        verticalAlignment = Alignment.CenterVertically // 높이 중앙 정렬
                    ) {
                        Text(
                            text = "이동할 폴더 선택",
                            style = ChaGokTextStyle.Title3,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        Text(
                            text = "새폴더",
                            style = ChaGokTextStyle.Title3,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                    }

                    // 폴더 리스트가 들어갈 자리
                    LazyColumn(
                        modifier = Modifier.weight(weight = 1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(folders) { folder ->
                            ChaGokFolderBox(
                                text = folder.name,
                                count = "{0}",
                                onClick = {

                                },
                            )
                        }
                    }

                    // 버튼을 하단에 고정하고 너비를 꽉 채우기
                    Button(
                        onClick = {
                            // 이동 로직 실행
                            showMoveBottomSheet = false
                            isEditMode = false
                            selectedIds = emptySet()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp) // 리스트와 버튼 사이 간격
                    ) {
                        Text("이동하기", style = ChaGokTextStyle.Subtitle1)
                    }
                }
            }
        }
    }
}

@Composable
fun FileVoiceNoteList(
    voiceNotes: List<VoiceNote>,
    isSelectionMode: Boolean, // 추가
    selectedIds: Set<UUID>,
    onSelectionChange: (Set<UUID>) -> Unit, // 추가
    onEditFolder: (VoiceNote) -> Unit,
    onDeleteFolder: (VoiceNote) -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. 상태 관리: 어떤 아이템이 열려 있는지 (ID로 관리)
    var revealedFileId by remember { mutableStateOf<UUID?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 14.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 2. 리스트 아이템 구성
        items(items = voiceNotes, key = { it.id }) { file ->
            ChaGokSwipeableFileItem(
                title = file.title,
                time = file.createdAt.formatTime(),
                duration = "{03:00}", // 예시
                isSelectionMode = isSelectionMode,
                isSelected = selectedIds.contains(file.id), // 이제 부모 상태가 잘 반영됨
                onChange = { isSelected ->
                    // 개별 아이템 체크/해제 시 부모에게 새로운 Set을 전달
                    val newSet = if (isSelected) {
                        selectedIds + file.id
                    } else {
                        selectedIds - file.id
                    }
                    onSelectionChange(newSet)
                },
                summary = SummaryStatus.COMPLETED,
                isRevealed = if (isSelectionMode) false else revealedFileId == file.id,
                onExpand = { if (!isSelectionMode) revealedFileId = file.id },
                onCollapse = { if (revealedFileId == file.id) revealedFileId = null },
                onEdit = { onEditFolder(file) },
                onDelete = { onDeleteFolder(file) },
                onClick = {},
            )
        }
    }
}


@Preview
@Composable
fun FileListScreenPreview() {
    FileListScreenContent(
        navController = rememberNavController(),
        uiState = StorageUiState(
            selectedFolderType = DefaultFolderType.RECENT, isLoading = false, errorMessage = null
        ),
        folderName = "TODO()",
        voiceNotes = emptyList(),
        onDeleteVoiceNote = { },
        onSortByCreate = { },
        onSortByUpdate = { },
        folders = emptyList(),
    )
}