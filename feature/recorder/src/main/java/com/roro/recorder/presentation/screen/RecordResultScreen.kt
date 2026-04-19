package com.roro.recorder.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.roro.core.navigation.Routes
import com.roro.core.ui.component.ChaGokAudioPlayer
import com.roro.recorder.domain.usecase.VoiceNoteResult
import com.roro.recorder.presentation.viewModel.RecordResultUiState
import com.roro.recorder.presentation.viewModel.RecordResultViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordResultScreen(
    navController: NavController,
    voiceNoteId: String,
    viewModel: RecordResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(voiceNoteId) {
        viewModel.load(voiceNoteId)
    }

    when (val state = uiState) {
        is RecordResultUiState.Loading -> RecordResultLoadingScreen()
        is RecordResultUiState.Error -> RecordResultErrorScreen(message = state.message)
        is RecordResultUiState.Success -> RecordResultContent(
            navController = navController,
            voiceNoteId = voiceNoteId,
            result = state.result,
            viewModel = viewModel
        )
    }
}

@Composable
private fun RecordResultContent(
    navController: NavController,
    voiceNoteId: String,
    result: VoiceNoteResult,
    viewModel: RecordResultViewModel
) {
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

    // 편집 모드 진입 시 키보드 올리기
    LaunchedEffect(isTitleEditing) {
        if (isTitleEditing) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val keyPoints = result.summaryText
        .split("*")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .take(3)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121218))
    ) {
        // 상단 TopBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "뒤로가기",
                    tint = Color.White
                )
            }

            // ✅ 편집 모드: TextField / 일반 모드: Text
            if (isTitleEditing) {
                BasicTextField(
                    value = editingTitle,
                    onValueChange = viewModel::onTitleChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            viewModel.confirmTitleEdit()
                        }
                    ),
                    cursorBrush = SolidColor(Color(0xFF9B7FD4))
                )
            } else {
                Text(
                    text = result.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.startTitleEdit() }
                )
            }

            // ✅ 편집 모드: [완료] 버튼 / 일반 모드: 검색 + 더보기
            if (isTitleEditing) {
                TextButton(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.confirmTitleEdit()
                    }
                ) {
                    Text(text = "완료", color = Color(0xFF9B7FD4), fontSize = 16.sp)
                }
            } else {
                IconButton(onClick = { navController.navigate(Routes.search(voiceNoteId)) }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "검색",
                        tint = Color.White
                    )
                }
                Box {
                    IconButton(onClick = { showDropdown = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "더보기",
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("기록 이동하기") },
                            onClick = { showDropdown = false }
                        )
                        DropdownMenuItem(
                            text = { Text("편집하기") },
                            onClick = {
                                showDropdown = false
                                navController.navigate(Routes.scriptEdit(voiceNoteId))
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("삭제하기", color = Color.Red) },
                            onClick = { showDropdown = false }
                        )
                    }
                }
            }
        }

        // 탭
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color(0xFF9B7FD4)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.5f)
                        )
                    }
                )
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
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFFB74D),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "스크립트가 수정되었어요. 요약을 다시 생성할까요?",
                    color = Color(0xFFFFB74D),
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
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
                    onRegenerate = viewModel::regenerateSummary
                )
                1 -> ScriptTab(
                    sttText = result.sttText,
                    currentPositionMs = playerState.currentPositionMs,
                    onSeek = viewModel::seekTo
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
    result: VoiceNoteResult,
    keyPoints: List<String>,
    isRegenerating: Boolean,
    isScriptModified: Boolean,
    onRegenerate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 폴더 + 날짜 + 길이
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "기본폴더",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp
                )
            }
            val dateText = buildString {
                append(formatDate(result.createdAt))
                if (result.updatedAt != result.createdAt) {
                    append(" (${formatDate(result.updatedAt)} 수정됨)")
                }
            }
            Text(
                text = dateText,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp
            )
            Text(
                text = formatDuration(result.durationSec),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp
            )
        }

        // 핵심 포인트
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "핵심 포인트",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                // 재생성 버튼 - 스크립트 수정 시 빨간 점, 재생성 중엔 로딩 표시
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .clickable(enabled = !isRegenerating) { onRegenerate() }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isRegenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Color(0xFF9B7FD4),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "재생성",
                                tint = Color(0xFF9B7FD4),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = if (isRegenerating) "재생성 중..." else "재생성",
                            color = Color(0xFF9B7FD4),
                            fontSize = 13.sp
                        )
                    }
                    // 스크립트 수정 후 빨간 점
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1E2E))
                        .padding(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF7B4FCC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = point,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 키워드
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "키워드",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(result.keywords) { keyword ->
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
}

@Composable
private fun ScriptTab(
    sttText: String,
    currentPositionMs: Long,
    onSeek: (Long) -> Unit
) {
    // \n 기준으로 세그먼트 파싱, index * 6000ms = startTimeMs
    val segments = remember(sttText) {
        sttText.split("\n")
            .mapIndexed { index, text -> Pair(index * 6000L, text) }
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
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        segments.forEachIndexed { index, (startMs, text) ->
            val isActive = index == activeIndex
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coords ->
                        itemHeights[index] = coords.size.height
                    }
                    .clickable { onSeek(startMs) }
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
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
private fun RecordResultLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121218)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = Color(0xFF9B7FD4))
            Text(text = "불러오는 중...", color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
private fun RecordResultErrorScreen(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121218)),
        contentAlignment = Alignment.Center
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