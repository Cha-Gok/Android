package com.roro.storage.presentation.filelist

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.roro.core.domain.model.FolderItem
import com.roro.core.domain.model.SortType
import com.roro.core.domain.model.VoiceNoteItem
import com.roro.core.navigation.Routes
import com.roro.core.navigation.SearchType
import com.roro.core.ui.component.ChaGokBackground
import com.roro.core.ui.component.ChaGokDialogCreateFolder
import com.roro.core.ui.component.ChaGokFolderBox
import com.roro.core.ui.component.ChaGokMenuItem
import com.roro.core.ui.component.ChaGokMoreMenu
import com.roro.core.ui.component.ChaGokSwipeableFileItem
import com.roro.core.ui.component.ChaGokTopBarV2
import com.roro.core.ui.component.SummaryStatus
import com.roro.core.ui.theme.ChaGokTextStyle
import com.roro.core.ui.theme.Danger
import com.roro.core.ui.theme.Gray100
import com.roro.core.ui.theme.Gray200
import com.roro.core.ui.theme.Gray300
import com.roro.core.ui.theme.PrimaryColor
import com.roro.core.ui.theme.TextDisabled
import com.roro.core.ui.theme.TextPrimary
import com.roro.core.ui.theme.TextSecondary
import com.roro.core.ui.theme.TextTertiary
import com.roro.core.util.formatDate
import com.roro.core.util.toUUIDOrNull
import com.roro.core.util.toast
import timber.log.Timber
import java.util.UUID

@Composable
fun FileListScreen(
    navController: NavController,
    folderName: String,
    folderId: String,
    isTrash: Boolean,
    viewModel: FileListViewModel = hiltViewModel()
) {
    Timber.d("folderId 확인 = $folderId")
    Timber.d("isTrah 확인 = $isTrash")
    Timber.d("folderName 확인 = $folderName")
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(folderId, folderName) {
        runCatching { UUID.fromString(folderId) }.getOrNull()?.let { uuid ->
            viewModel.onIntent(FileListIntent.Initialize(folderId = uuid, folderName = folderName))
        }
    }



    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                FileListEffect.NavigateToSearch -> {
                    navController.navigate(
                        Routes.searchTemp(
                            searchType = SearchType.VOICE_NOTE,
                            folderId = folderId
                        )
                    )
                }

                is FileListEffect.ShowToast -> context.toast(effect.message)
                FileListEffect.NavigateBack -> {
                    navController.popBackStack()
                }

                is FileListEffect.NavigateDetailVoiceNote -> {
                    navController.navigate(
                        Routes.recordResult(
                            voiceNoteId = effect.voiceNoteId
                        )
                    )
                }
            }
        }
    }

    BackHandler {
        viewModel.onIntent(FileListIntent.ClickBack)
    }


    FileListScreenContent(
        uiState = uiState,
        onIntent = viewModel::onIntent,
        isTrash = isTrash
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FileListScreenContent(
    uiState: FileListUiState,
    onIntent: (FileListIntent) -> Unit,
    isTrash: Boolean,
) {
    BackHandler(enabled = uiState.isSelectMode || uiState.isMenuExpanded || uiState.isBottomSheet) {
        if (uiState.isMenuExpanded) {
            onIntent(FileListIntent.ShowMoreMenu(false))
        } else if (uiState.isBottomSheet) {
            onIntent(FileListIntent.ShowBottomSheet(false))
        } else {
            onIntent(FileListIntent.ExitSelectionMode)
        }
    }

    ChaGokBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                ChaGokTopBarV2(
                    title = if (uiState.isSelectMode) "${uiState.selectedIds.size}개" else uiState.folderName,
                    showBackButton = true,
                    backButtonIcon = if (uiState.isSelectMode) Icons.Default.Close else Icons.Default.ArrowBackIosNew,
                    onBackClick = {
                        onIntent(FileListIntent.ClickBack)
                    },

                    firstActionIcon = if (!uiState.isSelectMode && !isTrash) Icons.Default.Search else null,
                    firstActionDescription = "search",
                    onFirstActionClick = { onIntent(FileListIntent.ClickSearch) },
                    secondActionIcon = if (!uiState.isSelectMode && !isTrash) Icons.Default.MoreVert else null,
                    secondActionDescription = "더보기",
                    onSecondActionClick = { onIntent(FileListIntent.ShowMoreMenu(true)) },
                    secondActionTrailingContent = {
                        if (uiState.isSelectMode) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { onIntent(FileListIntent.ShowBottomSheet(true)) }) {
                                    Text(text = "이동", color = TextPrimary, style = ChaGokTextStyle.Title2)
                                }
                                TextButton(
                                    onClick = {
                                        if (uiState.selectedIds.isNotEmpty()) {
                                            onIntent(FileListIntent.ShowDeleteDialog(true))
                                        }
                                    }
                                ) {
                                    Text(text = "삭제", color = Danger, style = ChaGokTextStyle.Title2)
                                }
                            }
                        } else if (!isTrash) {
                            ChaGokMoreMenu(
                                expanded = uiState.isMenuExpanded,
                                onDismissRequest = { onIntent(FileListIntent.ShowMoreMenu(false)) },
                                items = listOf(
                                    ChaGokMenuItem(
                                        text = "생성일 순",
                                        onClick = {
                                            onIntent(FileListIntent.ChangeSort(SortType.CREATED_AT))
                                        }
                                    ),
                                    ChaGokMenuItem(
                                        text = "수정일 순",
                                        onClick = {
                                            onIntent(FileListIntent.ChangeSort(SortType.UPDATED_AT))
                                        }
                                    ),
                                    ChaGokMenuItem(
                                        text = if (uiState.selectedIds.size == uiState.item.size && uiState.item.isNotEmpty()) "전체 해제" else "전체 선택",
                                        onClick = { onIntent(FileListIntent.ToggleSelectAll) }
                                    ),
                                    ChaGokMenuItem(
                                        text = "선택하기",
                                        onClick = { onIntent(FileListIntent.EnterSelectionMode) }
                                    )
                                )
                            )
                        }
                    }
                )

                FileVoiceNoteList(
                    voiceNotes = uiState.item,
                    isSelectionMode = uiState.isSelectMode,
                    selectedIds = uiState.selectedIds,
                    onItemClick = { voiceNote ->
                        if (uiState.isSelectMode) {
                            onIntent(FileListIntent.ToggleSelectItem(UUID.fromString(voiceNote.id)))
                        } else {
                            onIntent(FileListIntent.ClickVoiceNote(voiceNoteId = voiceNote.id))
                        }
                    }, // 콤마 추가 확인
                    onDeleteFile = { voiceNote ->
                        onIntent(FileListIntent.SwipeDeleteFile(voiceNoteItem = voiceNote))
                    }
                )
            }
        }


        // 1. 삭제 다이얼로그
        if (uiState.showDeleteDialog) {
            ChaGokDialogCreateFolder(
                title = "기록을 삭제할까요",
                description = "휴지통으로 이동되며,\n직접 비우기 전까지 보관돼요",
                dismissText = "취소",
                confirmText = "삭제",
                onDismiss = { onIntent(FileListIntent.ShowDeleteDialog(false)) },
                onConfirm = { onIntent(FileListIntent.ConfirmDelete) }
            )
        }

        val screenHeight = LocalConfiguration.current.screenHeightDp.dp

        // FileListScreenContent.kt 하단
        if (uiState.isBottomSheet) {
            val sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = { newValue ->
                    // newValue가 Hidden 상태(내려가는 상태)일 때 동작을 제어할 수 있습니다.
                    // 드래그해서 닫히는 것이 불편하다면 여기서 특정 조건에 따라 true/false를 반환합니다.
                    true
                }
            )

            ModalBottomSheet(
                modifier = Modifier
                    .fillMaxWidth(),
                onDismissRequest = { onIntent(FileListIntent.ShowBottomSheet(false)) },
                sheetState = sheetState,
                containerColor = Gray100,
            ) {
                AnimatedContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = screenHeight * 0.6f),
                    targetState = uiState.sheetMode,
                    transitionSpec = {
                        if (targetState == FileListSheetMode.CREATE_FOLDER) {
                            // 목록 -> 생성 (오른쪽에서 왼쪽으로 슬라이드)
                            slideInHorizontally { width -> width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> -width } + fadeOut()
                        } else {
                            // 생성 -> 목록 (왼쪽에서 오른쪽으로 슬라이드)
                            slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> width } + fadeOut()
                        }
                    },
                    label = "SheetModeAnimation"
                ) { targetMode ->
                    // 바텀시트 내부 콘텐츠
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(bottom = 32.dp, start = 20.dp, end = 20.dp)
                    ) {
                        when (targetMode) {
                            FileListSheetMode.FOLDER_LIST -> {
                                FolderListBottomSheet(
                                    folderList = uiState.folderList,
                                    selectedFolder = uiState.selectedFolder,
                                    onFolderSelect = { onIntent(FileListIntent.SelectTargetFolder(it)) },
                                    onConfirmMove = { onIntent(FileListIntent.ConfirmMove) },
                                    onCreateFolderClick = { onIntent(FileListIntent.ChangeSheetMode(FileListSheetMode.CREATE_FOLDER)) }
                                )
                            }

                            FileListSheetMode.CREATE_FOLDER -> {
                                NewFolderDialog(
                                    newFolderName = uiState.createFolderName,
                                    errorMessage = uiState.errorMessage,
                                    onNameChange = { onIntent(FileListIntent.UpdateNewFolderName(it)) },
                                    onCancel = { onIntent(FileListIntent.ChangeSheetMode(FileListSheetMode.FOLDER_LIST)) },
                                    onConfirm = { onIntent(FileListIntent.ConfirmCreateFolder) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FolderListBottomSheet(
    folderList: List<FolderItem>,
    selectedFolder: FolderItem?,
    onFolderSelect: (FolderItem) -> Unit,
    onConfirmMove: () -> Unit,
    onCreateFolderClick: () -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.SpaceBetween, // 양끝 정렬
            verticalAlignment = Alignment.CenterVertically // 높이 중앙 정렬
        ) {
            Text(
                text = "이동할 폴더 선택",
                style = ChaGokTextStyle.Title3,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Row(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .clickable { onCreateFolderClick() }, // 영역 전체 클릭 가능하게 변경
                verticalAlignment = Alignment.CenterVertically // ✅ 아이콘과 텍스트 수직 중앙 정렬
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "새 폴더",
                    style = ChaGokTextStyle.Title3,
                    color = TextSecondary
                )
            }

        }

        // 폴더 리스트가 들어갈 자리
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = screenHeight * 0.4f)
                .weight(weight = 1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(folderList) { folder ->
                ChaGokFolderBox(
                    text = folder.title,
                    count = folder.count,
                    onClick = { onFolderSelect(folder) },
                    isSelected = folder.id == selectedFolder?.id
                )
            }
        }

        // 버튼을 하단에 고정하고 너비를 꽉 채우기
        Button(
            onClick = onConfirmMove,
            enabled = selectedFolder != null,
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(20.dp),
            border = if (selectedFolder == null) BorderStroke(1.dp, TextDisabled) else null,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryColor,
                contentColor = TextPrimary,
                disabledContainerColor = Gray300.copy(alpha = 0.3f),
                disabledContentColor = TextDisabled
            )
        ) {
            Text(text = "이동하기", style = ChaGokTextStyle.Subtitle1)
        }
    }
}

@Composable
fun FileVoiceNoteList(
    voiceNotes: List<VoiceNoteItem>,
    isSelectionMode: Boolean,
    selectedIds: Set<UUID>,
    onItemClick: (VoiceNoteItem) -> Unit,
    onDeleteFile: (VoiceNoteItem) -> Unit,
    modifier: Modifier = Modifier
) {
    // 어떤 아이템이 스와이프되어 열려 있는지 관리
    var revealedFileId by remember { mutableStateOf<UUID?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 14.dp, start = 20.dp, end = 20.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = voiceNotes, key = { it.id }) { file ->
            ChaGokSwipeableFileItem(
                title = file.title,
                time = file.createdAt.formatDate(),
                duration = file.duration,
                summary = SummaryStatus.COMPLETED,

                // ✅ 선택 모드 관련 설정
                isSelectionMode = isSelectionMode,
                isSelected = selectedIds.contains(file.id.toUUIDOrNull()),
                onChange = {
                    // 체크박스 클릭 시 부모에게 전달
                    onItemClick(file)
                },

                // ✅ 스와이프 제어 (선택 모드일 때는 강제로 닫음)
                isRevealed = if (isSelectionMode) false else revealedFileId == file.id.toUUIDOrNull(),
                onExpand = {
                    if (!isSelectionMode) revealedFileId = file.id.toUUIDOrNull()
                },
                onCollapse = {
                    if (revealedFileId == file.id.toUUIDOrNull()) revealedFileId = null
                },
                onDelete = {
                    onDeleteFile(file)
                },

                // ✅ 아이템 본체 클릭
                onClick = {
                    onItemClick(file)
                }
            )
        }
    }
}

@Composable
private fun NewFolderDialog(
    newFolderName: String,
    errorMessage: String? = null,
    onNameChange: (String) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val maxLength = 50

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "새 폴더 만들기",
            style = ChaGokTextStyle.Title3,
            color = TextPrimary,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        BasicTextField(
            value = newFolderName,
            onValueChange = {
                if (it.length <= 50) {
                    onNameChange(it)
                }
            },
            textStyle = ChaGokTextStyle.Body1.copy(color = TextPrimary),
            cursorBrush = SolidColor(PrimaryColor),
            singleLine = true,
            decorationBox = { innerTextField ->
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(53.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Gray200)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 56.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (newFolderName.isEmpty()) {
                                Text(
                                    text = "폴더 이름",
                                    style = ChaGokTextStyle.Body1,
                                    color = TextTertiary
                                )
                            }
                            innerTextField()
                        }

                        Text(
                            text = "${newFolderName.length}/$maxLength",
                            style = ChaGokTextStyle.Body1,
                            color = TextTertiary,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                    if (!errorMessage.isNullOrBlank()) {
                        Text(
                            text = errorMessage,
                            style = ChaGokTextStyle.Label,
                            color = Danger,
                            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                        )
                    }
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 취소 시 다시 목록으로 돌아감
            Button(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(20.dp),
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gray300,
                    contentColor = TextTertiary
                )
            ) {
                Text(text = "취소", style = ChaGokTextStyle.Body1)
            }
            Button(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                onClick = onConfirm,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor,
                    contentColor = TextPrimary
                )
            ) {
                Text(text = "만들기", style = ChaGokTextStyle.Body1)
            }
        }
    }
}


@Preview
@Composable
fun FileListScreenPreview() {
    FileListScreenContent(
        uiState = FileListUiState(),
        onIntent = {},
        isTrash = false
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E1E) // 배경색 지정 가능
@Composable
fun NewFolderDialogPreview() {
    Box(modifier = Modifier.padding(20.dp)) {
        NewFolderDialog(
            newFolderName = "테스트 폴더",
            onNameChange = {},
            onCancel = {},
            onConfirm = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1E1E1E)
@Composable
fun FolderListBottomSheetPreview() {
    val mockFolders = listOf(
        FolderItem(
            id = UUID.randomUUID().toString(),
            title = "기본 폴더",
            count = "1",
            createAt = 1776769324521
        ), // <--- 쉼표 추가 및 괄호 닫기
        FolderItem(
            id = UUID.randomUUID().toString(),
            title = "중요 문서",
            count = "5",
            createAt = 1776769342203
        ), // <--- 쉼표 추가
        FolderItem(
            id = UUID.randomUUID().toString(),
            title = "아이디어 기록",
            count = "10",
            createAt = 1776771640900
        )
    )

    Box(modifier = Modifier.padding(20.dp)) {
        FolderListBottomSheet(
            folderList = mockFolders,
            selectedFolder = mockFolders[0],
            onFolderSelect = {},
            onConfirmMove = {},
            onCreateFolderClick = {}
        )
    }
}