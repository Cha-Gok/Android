package com.roro.recorder.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.roro.core.domain.model.FileListSheetMode
import com.roro.core.domain.model.FolderItem
import com.roro.core.navigation.Routes
import com.roro.core.ui.component.ChaGokAudioPlayer
import com.roro.core.ui.component.ChaGokDialogCreateFolder
import com.roro.core.ui.component.ChaGokFolderBox
import com.roro.core.ui.component.ChaGokMenuItem
import com.roro.core.ui.component.ChaGokMoreMenu
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
import com.roro.recorder.domain.usecase.VoiceNoteResult
import com.roro.recorder.presentation.RecordViewModel
import com.roro.recorder.presentation.viewModel.RecordResultEffect
import com.roro.recorder.presentation.viewModel.RecordResultIntent
import com.roro.recorder.presentation.viewModel.RecordResultIntent.ClickMoveToFolder
import com.roro.recorder.presentation.viewModel.RecordResultIntent.ShowMoreMenu
import com.roro.recorder.presentation.viewModel.RecordResultUiState
import com.roro.recorder.presentation.viewModel.RecordResultViewModel
import com.roro.recorder.presentation.viewModel.SummaryDisplayState
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel


@Composable
fun RecordResultScreen(
    navController: NavController,
  voiceNoteId: String, 
  viewModel: RecordResultViewModel = hiltViewModel(), 
  recordViewModel: RecordViewModel = hiltViewModel()
) {
    // ViewModel 상태 구독
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // voiceNoteId 있으면 바로 로드, 없으면 navigationEvent 기다림
    LaunchedEffect(voiceNoteId) {
        if (voiceNoteId.isNotEmpty()) {
            viewModel.load(voiceNoteId)
        }
    }


    when (val state = uiState) {
        is RecordResultUiState.Loading -> RecordResultLoadingScreen()
        is RecordResultUiState.Error -> RecordResultErrorScreen(message = state.message)
        is RecordResultUiState.Success -> RecordResultContent(
            navController = navController, voiceNoteId = voiceNoteId, result = state.result, state = state, viewModel = viewModel
        )

        is RecordResultUiState.NoSpeech -> RecordResultContent(
            navController = navController, voiceNoteId = voiceNoteId, result = state.result, viewModel = viewModel, state = state, summaryState = SummaryDisplayState.NoSpeech
        )

        is RecordResultUiState.SummaryError -> RecordResultContent(
            navController = navController, voiceNoteId = voiceNoteId, result = state.result, viewModel = viewModel, state = state, summaryState = SummaryDisplayState.Error
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordResultContent(
    navController: NavController,
    voiceNoteId: String,
    result: VoiceNoteResult,
    viewModel: RecordResultViewModel,
    state: RecordResultUiState,
    summaryState: SummaryDisplayState = SummaryDisplayState.Success
) {
    // 1. 모든 상태(Success, NoSpeech, SummaryError)에서 공통 필드 추출
    val contentState = state as? RecordResultUiState.ContentState

    // 2. 다이얼로그 노출 여부 (공통 필드에서 가져옴)
    val showDeleteDialog = contentState?.showDeleteDialog ?: false
    val isBottomSheet = contentState?.isBottomSheet ?: false

    val sheetMode = contentState?.sheetMode ?: FileListSheetMode.FOLDER_LIST

    // ✅ 로그 추가: 현재 UI가 알고 있는 상태를 Logcat에 출력
    LaunchedEffect(state, isBottomSheet, sheetMode) {
        Timber.d("===== RecordResult UI Debug =====")
        Timber.d("현재 UiState 타입: ${state::class.simpleName}")
        Timber.d("바텀시트 노출 여부 (isBottomSheet): $isBottomSheet")
        Timber.d("바텀시트 모드 (sheetMode): $sheetMode")
        Timber.d("폴더 리스트 개수: ${contentState?.folderList?.size ?: 0}")
        Timber.d("==================================")
    }


// 3. 로그 찍기 (다이얼로그 전용)
    LaunchedEffect(showDeleteDialog) {
        if (showDeleteDialog) Timber.d("삭제 다이얼로그 활성화됨")
    }


    // 폴더 목록 등 필요한 나머지 값들도 동일하게 추출
    val folderList =
        (state as? RecordResultUiState.Success)?.folderList ?: (state as? RecordResultUiState.NoSpeech)?.folderList ?: (state as? RecordResultUiState.SummaryError)?.folderList ?: emptyList()

    val selectedFolder =
        (state as? RecordResultUiState.Success)?.selectedFolder ?: (state as? RecordResultUiState.NoSpeech)?.selectedFolder ?: (state as? RecordResultUiState.SummaryError)?.selectedFolder

    val createFolderName =
        (state as? RecordResultUiState.Success)?.createFolderName ?: (state as? RecordResultUiState.NoSpeech)?.createFolderName ?: (state as? RecordResultUiState.SummaryError)?.createFolderName ?: ""

    val errorMessage = (state as? RecordResultUiState.Success)?.errorMessage ?: (state as? RecordResultUiState.NoSpeech)?.errorMessage ?: (state as? RecordResultUiState.SummaryError)?.errorMessage


    val successState = state as? RecordResultUiState.Success

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    // ── 1. 삭제 확인 다이얼로그 ──
    if (showDeleteDialog) {
        ChaGokDialogCreateFolder(
            title = "기록을 삭제할까요",
            description = "휴지통으로 이동되며,\n직접 비우기 전까지 보관돼요",
            dismissText = "취소",
            confirmText = "삭제",
            onDismiss = { viewModel.onIntent(RecordResultIntent.ShowDeleteDialog(false)) }, // import.* 되어 있으므로 간소화
            onConfirm = { viewModel.onIntent(RecordResultIntent.ConfirmDelete) })
    }


    var selectedTab by remember { mutableIntStateOf(0) }
    var showDropdown by remember { mutableStateOf(false) }
    val tabs = listOf("요약", "스크립트")

    val playerState by viewModel.playerUiState.collectAsStateWithLifecycle()
    val isRegenerating by viewModel.isRegenerating.collectAsStateWithLifecycle()
    val isScriptModified by viewModel.isScriptModified.collectAsStateWithLifecycle()  // ✅
    val isTitleEditing by viewModel.isTitleEditing.collectAsStateWithLifecycle()
    val editingTitle by viewModel.editingTitle.collectAsStateWithLifecycle()

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // ── 2. 이동 바텀시트 (FileListScreen 로직 이식) ──
    if (isBottomSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            modifier = Modifier.fillMaxWidth(),
            onDismissRequest = { viewModel.onIntent(RecordResultIntent.ShowBottomSheet(false)) },
            sheetState = sheetState,
            containerColor = Gray100,
        ) {
            AnimatedContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = screenHeight * 0.6f), targetState = sheetMode, transitionSpec = {
                    if (targetState == FileListSheetMode.CREATE_FOLDER) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith slideOutHorizontally { width -> width } + fadeOut()
                    }
                }, label = "SheetModeAnimation"
            ) { targetMode ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(bottom = 32.dp, start = 20.dp, end = 20.dp)
                ) {
                    when (targetMode) {
                        FileListSheetMode.FOLDER_LIST -> {
                            FolderListBottomSheet(folderList = folderList, selectedFolder = selectedFolder, onFolderSelect = { folder ->
                                // ✅ 수정: 폴더를 클릭하면 해당 폴더가 선택되어야 함
                                viewModel.onIntent(RecordResultIntent.SelectTargetFolder(folder))
                            }, onConfirmMove = {
                                // ✅ 이동 확정
                                viewModel.onIntent(RecordResultIntent.ConfirmMove)
                            }, onCreateFolderClick = {
                                // ✅ 새 폴더 만들기 모드로 전환
                                viewModel.onIntent(RecordResultIntent.ChangeSheetMode(FileListSheetMode.CREATE_FOLDER))
                            })
                        }

                        FileListSheetMode.CREATE_FOLDER -> {
                            NewFolderDialog(
                                newFolderName = createFolderName,
                                errorMessage = errorMessage,
                                onNameChange = { viewModel.onIntent(RecordResultIntent.UpdateNewFolderName(it)) },
                                onCancel = {
                                    // 취소 시 다시 폴더 목록으로
                                    viewModel.onIntent(RecordResultIntent.ChangeSheetMode(FileListSheetMode.FOLDER_LIST))
                                },
                                onConfirm = {
                                    // ✅ 새 폴더 생성 확정
                                    viewModel.onIntent(RecordResultIntent.ConfirmCreateFolder)
                                })
                        }
                    }
                }
            }
        }
    }

    // RecordResultScreen 최상단 부근에 추가
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RecordResultEffect.NavigateBack -> {
                    navController.popBackStack() // 삭제 성공 시 목록으로 이동
                }

                is RecordResultEffect.ShowToast -> {
                    // context.toast(effect.message) 등 토스트 노출
                }
            }
        }
    }

    // 편집 모드 진입 시 키보드 올리기
    LaunchedEffect(isTitleEditing) {
        if (isTitleEditing) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val keyPoints = result.summaryText.split("*").map { it.trim() }.filter { it.isNotBlank() }.take(3)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121218))
    ) {
        // 상단 TopBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew, contentDescription = "뒤로가기", tint = Color.White
                )
            }

            // ✅ 편집 모드: TextField / 일반 모드: Text
            if (isTitleEditing) {
                BasicTextField(
                    value = editingTitle, onValueChange = viewModel::onTitleChange, modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester), textStyle = TextStyle(
                        color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold
                    ), singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            viewModel.confirmTitleEdit()
                        }), cursorBrush = SolidColor(Color(0xFF9B7FD4))
                )
            } else {
                Text(
                    text = result.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.startTitleEdit() })
            }

            // ✅ 편집 모드: [완료] 버튼 / 일반 모드: 검색 + 더보기
            if (isTitleEditing) {
                TextButton(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.confirmTitleEdit()
                    }) {
                    Text(text = "완료", color = Color(0xFF9B7FD4), fontSize = 16.sp)
                }
            } else {
                IconButton(onClick = { navController.navigate(Routes.SEARCH) }) {
                    Icon(
                        imageVector = Icons.Default.Search, contentDescription = "검색", tint = Color.White
                    )
                }
                Box {
                    IconButton(onClick = {
                        // 로컬 변수 showDropdown = true 대신 ViewModel Intent 사용
                        viewModel.onIntent(RecordResultIntent.ShowMoreMenu(true))
                    }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert, contentDescription = "더보기", tint = Color.White
                        )
                    }

                    ChaGokMoreMenu(
                        // 상단에서 추출한 공통 contentState의 expanded 상태 사용
                        expanded = contentState?.isMenuExpanded == true, onDismissRequest = {
                            viewModel.onIntent(RecordResultIntent.ShowMoreMenu(false))
                        }, items = listOf(
                            ChaGokMenuItem(
                            text = "기록 이동하기", onClick = {
                                viewModel.onIntent(ShowMoreMenu(false))
                                viewModel.onIntent(ClickMoveToFolder)
                            }), ChaGokMenuItem(
                            text = "편집하기", onClick = {
                                viewModel.onIntent(RecordResultIntent.ShowMoreMenu(false))
                                navController.navigate(Routes.scriptEdit(voiceNoteId))
                            }), ChaGokMenuItem(
                            text = "삭제하기", textColor = Color.Red, // Danger 색상이 있다면 Danger 사용
                            onClick = {
                                viewModel.onIntent(RecordResultIntent.ShowMoreMenu(false))
                                viewModel.onIntent(RecordResultIntent.ShowDeleteDialog(true))
                            })))
                }
            }
        }

        // 탭
        TabRow(
            selectedTabIndex = selectedTab, containerColor = Color.Transparent, contentColor = Color.White, indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = Color(0xFF9B7FD4)
                )
            }) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = {
                    Text(
                        text = title, color = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.5f)
                    )
                })
            }
        }

        // 스크립트 수정 후 배너
        if (isScriptModified) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2A1F1F))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "스크립트가 수정되었어요. 요약을 다시 생성할까요?", color = Color(0xFFFFB74D), fontSize = 13.sp, modifier = Modifier.weight(1f)
                )
            }
        }

        // 탭 콘텐츠
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> AiSummaryTab(
                    result = result,
                    keyPoints = keyPoints,
                    isRegenerating = isRegenerating,
                    isScriptModified = isScriptModified,
                    onRegenerate = viewModel::regenerateSummary,
                    summaryState = summaryState
                )

                1 -> ScriptTab(
                    sttText = result.sttText, currentPositionMs = playerState.currentPositionMs, onSeek = viewModel::seekTo
                )
            }
        }

        // 탭 변경과 무관하게 하단 고정 플레이어
        ChaGokAudioPlayer(
            currentPositionMs = playerState.currentPositionMs,
            durationMs = playerState.durationMs,
            isPlaying = playerState.isPlaying,
            onPlay = viewModel::play,
            onPause = viewModel::pause,
            onSeek = viewModel::seekTo,
            onRewind = viewModel::rewind5,
            onForward = viewModel::forward5
        )
    }
}

@Composable
private fun AiSummaryTab(
    result: VoiceNoteResult, keyPoints: List<String>, isRegenerating: Boolean, isScriptModified: Boolean, onRegenerate: () -> Unit, summaryState: SummaryDisplayState = SummaryDisplayState.Success
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 폴더 + 날짜 + 길이
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Folder, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "기본폴더", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp
                )
            }
            val dateText = buildString {
                append(formatDate(result.createdAt))
                if (result.updatedAt != result.createdAt) {
                    append(" (${formatDate(result.updatedAt)} 수정됨)")
                }
            }
            Text(text = dateText, color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
            Text(text = formatDuration(result.durationSec), color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
        }

        // 상태에 따라 분기
        when (summaryState) {
            SummaryDisplayState.Success -> {
                // 핵심 포인트
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "핵심 포인트", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold
                        )
                        Box {
                            Row(modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .clickable(enabled = !isRegenerating) { onRegenerate() }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (isRegenerating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp), color = Color(0xFF9B7FD4), strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh, contentDescription = "재생성", tint = Color(0xFF9B7FD4), modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = if (isRegenerating) "재생성 중..." else "재생성", color = Color(0xFF9B7FD4), fontSize = 13.sp
                                )
                            }
                            if (isScriptModified && !isRegenerating) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF4444))
                                        .align(Alignment.TopEnd)
                                )
                            }
                        }
                    }

                    keyPoints.forEachIndexed { index, point ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E1E2E))
                                .padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF7B4FCC)), contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = point, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 키워드
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "키워드", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        result.keywords.forEach { keyword ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(Color(0xFF2D2D3A))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = keyword, color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            SummaryDisplayState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp), contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "요약을 생성하지 못했어요.", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "일시적인 오류가 발생했어요.\n잠시 후 다시 시도해주세요.", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp, textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFF9B7FD4))
                            .clickable { onRegenerate() }
                            .padding(horizontal = 24.dp, vertical = 12.dp)) {
                            Text(text = "재생성", color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }

            SummaryDisplayState.NoSpeech -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp), contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MicOff, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "요약할 수 있는 음성이\n기록되지 않았어요.", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                        )
                        Text(
                            text = "인식된 음성이 없어\n요약을 생성할 수 없어요.", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp, textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScriptTab(
    sttText: String, currentPositionMs: Long, onSeek: (Long) -> Unit
) {
    // \n 기준으로 세그먼트 파싱, index * 6000ms = startTimeMs
    val segments = remember(sttText) {
        sttText.split("\n").mapIndexed { index, text -> Pair(index * 30000L, text) }  // 7000 → 30000
            .filter { it.second.isNotBlank() }
    }

    // 현재 재생 위치에 해당하는 세그먼트 인덱스
    val activeIndex = remember(currentPositionMs) {
        val idx = segments.indexOfLast { (startMs, _) -> currentPositionMs >= startMs }
        if (idx < 0) 0 else idx
    }

    val scrollState = rememberScrollState()
    val itemHeights = remember { mutableStateMapOf<Int, Int>() }

    // 활성 세그먼트로 자동 스크롤
    LaunchedEffect(activeIndex) {
        val scrollTo = (0 until activeIndex).sumOf { itemHeights[it] ?: 0 }
        scrollState.animateScrollTo(scrollTo)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        segments.forEachIndexed { index, (startMs, text) ->
            val isActive = index == activeIndex
            Column(modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coords ->
                    itemHeights[index] = coords.size.height
                }
                .clickable { onSeek(startMs) }
                .padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // 타임스탬프
                Text(
                    text = formatTime(startMs),
                    color = if (isActive) Color(0xFF9B7FD4) else Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
                // 세그먼트 텍스트
                Text(
                    text = text,
                    color = if (isActive) Color.White else Color.White.copy(alpha = 0.6f),
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}

// ── 유틸: ms → "00:00" 형식 ──────────────────────────────────────────────

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
internal fun RecordResultLoadingScreen() {
    val shimmerColors = listOf(
        Color(0xFF2A2A3A), Color(0xFF3A3A4E), Color(0xFF2A2A3A)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f, targetValue = 1000f, animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing), repeatMode = RepeatMode.Restart
        ), label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors, start = Offset(translateAnim - 300f, 0f), end = Offset(translateAnim, 0f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121218))
    ) {
        // ── TopBar ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            // 뒤로가기 아이콘 자리
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(brush)
            )
            Spacer(modifier = Modifier.width(8.dp))
            // 제목 자리
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.width(8.dp))
            // 검색 + 더보기 아이콘 자리
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(brush)
                    )
                }
            }
        }

        // ── TabRow ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            repeat(2) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF2A2A3A))
        )

        // ── 콘텐츠 ─────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 폴더 / 날짜 / 길이
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }

            // 핵심 포인트 헤더 + 재생성 버튼
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(brush)
                )
            }

            // 핵심 포인트 카드 3개
            repeat(3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1E2E))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // 번호 원
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(brush)
                    )
                    // 텍스트 2줄
                    Column(
                        modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(13.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(brush)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(13.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(brush)
                        )
                    }
                }
            }

            // 키워드 섹션
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(56, 72, 48, 64).forEach { width ->
                        Box(
                            modifier = Modifier
                                .width(width.dp)
                                .height(28.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(brush)
                        )
                    }
                }
            }
        }

        // ── 하단 오디오 플레이어 ────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A26))
                .padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 프로그레스 바
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(brush)
            )
            // 버튼 3개 + 시간 표시
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(brush)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }
        }
    }
}

@Composable
fun FolderListBottomSheet(
    folderList: List<FolderItem>, selectedFolder: FolderItem?, onFolderSelect: (FolderItem) -> Unit, onConfirmMove: () -> Unit, onCreateFolderClick: () -> Unit
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(), horizontalArrangement = Arrangement.SpaceBetween, // 양끝 정렬
            verticalAlignment = Alignment.CenterVertically // 높이 중앙 정렬
        ) {
            Text(
                text = "이동할 폴더 선택", style = ChaGokTextStyle.Title3, color = TextPrimary, modifier = Modifier.padding(bottom = 24.dp)
            )

            Row(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .clickable { onCreateFolderClick() }, // 영역 전체 클릭 가능하게 변경
                verticalAlignment = Alignment.CenterVertically // ✅ 아이콘과 텍스트 수직 중앙 정렬
            ) {
                Icon(
                    imageVector = Icons.Default.Add, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "새 폴더", style = ChaGokTextStyle.Title3, color = TextSecondary
                )
            }

        }

        // 폴더 리스트가 들어갈 자리
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = screenHeight * 0.4f)
                .weight(weight = 1f, fill = false), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(folderList) { folder ->
                ChaGokFolderBox(
                    text = folder.title, count = folder.count, onClick = { onFolderSelect(folder) }, isSelected = folder.id == selectedFolder?.id
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
                containerColor = PrimaryColor, contentColor = TextPrimary, disabledContainerColor = Gray300.copy(alpha = 0.3f), disabledContentColor = TextDisabled
            )
        ) {
            Text(text = "이동하기", style = ChaGokTextStyle.Subtitle1)
        }
    }
}

@Composable
private fun NewFolderDialog(
    newFolderName: String, errorMessage: String? = null, onNameChange: (String) -> Unit, onCancel: () -> Unit, onConfirm: () -> Unit
) {
    val maxLength = 50

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "새 폴더 만들기", style = ChaGokTextStyle.Title3, color = TextPrimary, modifier = Modifier.padding(vertical = 24.dp)
        )

        BasicTextField(value = newFolderName, onValueChange = {
            if (it.length <= 50) {
                onNameChange(it)
            }
        }, textStyle = ChaGokTextStyle.Body1.copy(color = TextPrimary), cursorBrush = SolidColor(PrimaryColor), singleLine = true, decorationBox = { innerTextField ->
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(53.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Gray200)
                        .padding(horizontal = 16.dp), contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 56.dp), contentAlignment = Alignment.CenterStart
                    ) {
                        if (newFolderName.isEmpty()) {
                            Text(
                                text = "폴더 이름", style = ChaGokTextStyle.Body1, color = TextTertiary
                            )
                        }
                        innerTextField()
                    }

                    Text(
                        text = "${newFolderName.length}/$maxLength", style = ChaGokTextStyle.Body1, color = TextTertiary, modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage, style = ChaGokTextStyle.Label, color = Danger, modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                    )
                }
            }
        })

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 취소 시 다시 목록으로 돌아감
            Button(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp), shape = RoundedCornerShape(20.dp), onClick = onCancel, colors = ButtonDefaults.buttonColors(
                    containerColor = Gray300, contentColor = TextTertiary
                )
            ) {
                Text(text = "취소", style = ChaGokTextStyle.Body1)
            }
            Button(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp), onClick = onConfirm, shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryColor, contentColor = TextPrimary
                )
            ) {
                Text(text = "만들기", style = ChaGokTextStyle.Body1)
            }
        }
    }
}

// 오류 발생 화면 -> 수정 예정
@Composable
private fun RecordResultErrorScreen(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121218)), contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = Color.White, fontSize = 14.sp)
    }
}

// ── 유틸: timestamp → "2026.03.02 · 오후 14:32" ──────────────────────────

private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("yyyy.MM.dd · a HH:mm", Locale.KOREAN)
    return dateFormat.format(Date(timestamp))
}

// ── 유틸: 초 → "1시간 12분 30초" ─────────────────────────────────────────

private fun formatDuration(durationSec: Double): String {
    val total = durationSec.toLong()
    val hours = total / 3600
    val minutes = (total % 3600) / 60
    val seconds = total % 60
    return buildString {
        if (hours > 0) append("${hours}시간 ")
        if (minutes > 0) append("${minutes}분 ")
        append("${seconds}초")
    }
}


// ── 프리뷰 ─────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFF121218)
@Composable
fun PreviewRecordResultDetail() {
    // 1. 가짜 데이터 생성 (제시해주신 VoiceNoteResult 구조 반영)
    val mockResult = VoiceNoteResult(
        title = "2026년 신규 프로젝트 브레인스토밍",
        sttText = "안녕하세요, 오늘 회의에서는 차곡차곡 앱의 신규 기능인 AI 자동 요약 기능에 대해 논의하겠습니다. 사용자가 녹음을 마치면 즉시 텍스트로 변환되고 요약본이 생성되어야 합니다.",
        summaryText = "• 사용자의 음성 기록을 AI가 자동으로 요약하는 기능 논의\n• 녹음 완료 즉시 STT 변환 및 요약 생성 프로세스 구축\n• 사용자 편의성을 위한 키워드 추출 시스템 도입",
        keywords = listOf("AI 요약", "STT", "사용자 경험", "신규 기능"),
        audioPath = "/sdcard/dummy.mp3",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        durationSec = 125.0 // 2분 5초
    )

    // 2. 프리뷰용 테마 설정
    MaterialTheme {
        // RecordResultContent가 ViewModel을 안 받는 구조라면 바로 호출 가능
        // 만약 ViewModel을 받는다면, 내부의 Stateless한 컴포저블(예: AiSummaryTab)을 호출하세요.
        Column(modifier = Modifier.fillMaxSize()) {
            AiSummaryTab(
                result = mockResult, keyPoints = mockResult.keywords, // 키워드 리스트
                isRegenerating = false, isScriptModified = false, onRegenerate = { /* 프리뷰이므로 비워둠 */ })
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121218)
@Composable
fun PreviewRecordResultLoading() {
    MaterialTheme {
        RecordResultLoadingScreen()
    }
}