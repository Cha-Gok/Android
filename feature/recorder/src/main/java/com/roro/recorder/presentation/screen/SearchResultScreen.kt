package com.roro.recorder.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.roro.recorder.domain.usecase.VoiceNoteResult
import com.roro.recorder.presentation.viewModel.SearchMatch
import com.roro.recorder.presentation.viewModel.SearchResultViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 수정 확인 사항
// 1. 기본 ismatch -> 단락 강조 없음, 스크립트 좌측 상단에 '스크립트' 표시
// 2. iscurrent -> 바탕 보라색 -> 주황색, 글자색 흰색 -> 검정 변경되도록
// 3. 바텀 바(업다운) 디자인 수정
// 4. 검색 바 디자인 수정
// 5. 핵심 포인트 1,2,3 나누기(RecordResultScreen와 동일하게).
// 5. 키워드 기본 디자인에서 타원 강조가 아니라 글자 강조로 변경(다른 부분과 동일하게)
// 6. 상단 탭에 숫자 강조 제거

// 7. 동일 단락 내에 검색 단어 여러 개인 경우 확인

@Composable
fun SearchResultScreen(
    navController: NavController,
    result: VoiceNoteResult,
    onSeek: (Long) -> Unit = {},
    viewModel: SearchResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val tabs = listOf("요약", "스크립트")

    LaunchedEffect(Unit) {
        viewModel.init(result.summaryText, result.sttText, result.keywords)
        focusRequester.requestFocus()
    }

    val currentMatches = if (uiState.selectedTab == 0)
        uiState.summaryMatches else uiState.scriptMatches

    LaunchedEffect(uiState.currentMatchIndex, uiState.selectedTab) {
        if (currentMatches.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(uiState.currentMatchIndex)
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121218))
            .imePadding(),  // ✅ 키보드 올라올 때 패딩 자동 조정
        containerColor = Color(0xFF121218),
        topBar = {
            Column {
                // ── 검색 TopBar ───────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {

                    // ✅ 검색바 (아이콘 포함, X는 밖으로)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .padding(end = 6.dp, start = 6.dp)
                            .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(50.dp))
                            .border(
                                width = 0.5.dp,
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(50.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp)
                        )
                        BasicTextField(
                            value = uiState.query,
                            onValueChange = { viewModel.onQueryChange(it, search = false) }, // 텍스트만 업데이트
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                            cursorBrush = SolidColor(Color(0xFF9B7FD4)),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = { viewModel.onSearch() }  // 검색 실행
                            ),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.CenterStart) {
                                    if (uiState.query.isEmpty()) {
                                        Text(
                                            text = "검색",
                                            color = Color.White.copy(alpha = 0.3f),
                                            fontSize = 15.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            },
                            singleLine = true
                        )
                    }

                    // X 버튼 (바깥)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.Black.copy(alpha = 0.8f), CircleShape)
                            .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "지우기",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // ── 탭 ────────────────────────────────────────────────
                TabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                            color = Color(0xFF9B7FD4)
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        val count = if (uiState.query.isBlank()) 0
                        else if (index == 0) uiState.summaryMatches.size
                        else uiState.scriptMatches.size

                        Tab(
                            selected = uiState.selectedTab == index,
                            onClick = { viewModel.onTabSelected(index) },
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = title,
                                        color = if (uiState.selectedTab == index) Color.White
                                        else Color.White.copy(alpha = 0.5f)
                                    )
                                    if (count > 0) {
                                        Box(
                                            modifier = Modifier
                                               // .background(Color(0xFF9B7FD4), CircleShape)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "$count",
                                                color = Color(0xFF9B7FD4),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        },
        // ✅ 하단 네비게이션 바 분리

        // Scaffold의 content 부분 (innerPadding 블록) 전체 교체
    ) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            // ── 기존 콘텐츠 ──
            when {
                uiState.query.isBlank() || currentMatches.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "검색어 입력 후 검색해주세요",
                            color = Color.White.copy(alpha = 0.3f),
                            fontSize = 14.sp
                        )
                    }
                }


                uiState.selectedTab == 0 -> {
                    SummarySearchResult(
                        result = result,
                        query = uiState.query,
                        summaryMatches = uiState.summaryMatches,
                        currentMatchIndex = uiState.currentMatchIndex
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 12.dp,
                            bottom = 80.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "스크립트",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        // 기존 itemsIndexed(currentMatches) 블록 → 아래로 교체
                        val groupedMatches = currentMatches.groupBy { it.segmentIndex }
                        groupedMatches.entries.forEachIndexed { groupIdx, (_, matches) ->
                            val isCurrentGroup = matches.any {
                                currentMatches.indexOf(it) == uiState.currentMatchIndex
                            }
                            item(key = matches.first().segmentIndex) {
                                ScriptSegmentItem(
                                    matches = matches,
                                    isCurrent = isCurrentGroup,
                                    currentMatchIndex = uiState.currentMatchIndex,
                                    allMatches = currentMatches,
                                    onClick = { onSeek(matches.first().startTimeMs) }
                                )
                            }
                        }
                    }
                }
            }

            // ── 플로팅 바텀바 ──
            if (currentMatches.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .fillMaxWidth()
                        .background(
                            color = Color.Black.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(50.dp)
                        )
                        .border(
                            width = 0.5.dp,
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = viewModel::prevMatch, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "이전",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "${uiState.currentMatchIndex + 1} / ${currentMatches.size}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = viewModel::nextMatch, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "다음",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}



// ── 검색 결과 아이템 ──────────────────────────────────────────────────────────


@Composable
private fun ScriptSegmentItem(
    matches: List<SearchMatch>,
    isCurrent: Boolean,
    currentMatchIndex: Int,
    allMatches: List<SearchMatch>,
    onClick: () -> Unit
) {
    val segText = matches.first().text
    val startTimeMs = matches.first().startTimeMs
    val sortedMatches = matches.sortedBy { it.matchStart }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Text(
            text = formatSearchTime(startTimeMs),
//            color = if (isCurrent) Color(0xFF9B7FD4) else Color.White.copy(alpha = 0.4f),
            fontSize = 12.sp,
//            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal

            color = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Normal
        )

        Text(
            text = buildAnnotatedString {
                var cursor = 0
                sortedMatches.forEach { match ->
                    val globalIdx = allMatches.indexOf(match)
                    val isThisCurrent = globalIdx == currentMatchIndex

                    if (match.matchStart > cursor) {
                        append(segText.substring(cursor, match.matchStart))
                    }
                    withStyle(
                        SpanStyle(
                            color = if (isThisCurrent) Color.Black else Color.White,
                            background = if (isThisCurrent) Color(0xFFFF9500) else Color(0xFF9B7FD4),
                        )
                    ) {
                        append(segText.substring(match.matchStart, match.matchEnd))
                    }
                    cursor = match.matchEnd
                }
                if (cursor < segText.length) {
                    append(segText.substring(cursor))
                }
            },
            fontSize = 14.sp,
            lineHeight = 22.sp,
            color = Color.White.copy(alpha = 1f)
        )
    }
}

@Composable
private fun SummarySearchResult(
    result: VoiceNoteResult,
    query: String,
    summaryMatches: List<SearchMatch>,
    currentMatchIndex: Int
) {
    val keyPoints = result.summaryText
        .split("*")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .take(3)

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
                Text(text = "기본폴더", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
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

        // 핵심 포인트
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "핵심 포인트",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            keyPoints.forEachIndexed { index, point ->
                val pointMatches = summaryMatches.filter { it.segmentIndex == index }
                val isCurrent = summaryMatches.getOrNull(currentMatchIndex)?.segmentIndex == index

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
                        text = buildAnnotatedString {
                            if (pointMatches.isEmpty()) {
                                append(point)
                            } else {
                                var cursor = 0
                                pointMatches.forEach { match ->
                                    val globalIdx = summaryMatches.indexOf(match)
                                    val isThisCurrent = globalIdx == currentMatchIndex

                                    if (match.matchStart > cursor) {
                                        append(point.substring(cursor, match.matchStart))
                                    }
                                    withStyle(SpanStyle(
                                        color = if (isThisCurrent) Color.Black else Color.White,
                                        background = if (isThisCurrent) Color(0xFFFF9500) else Color(0xFF9B7FD4),
                                    )) {
                                        append(point.substring(match.matchStart, match.matchEnd))
                                    }
                                    cursor = match.matchEnd

                                }
                                if (cursor < point.length) append(point.substring(cursor))
                            }
                        },
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
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                result.keywords.forEachIndexed { kwIndex, keyword ->
                    // segmentIndex 100+ 가 키워드
                    val kwMatches = summaryMatches.filter { it.segmentIndex == 100 + kwIndex }
                    val currentMatch = summaryMatches.getOrNull(currentMatchIndex)
                    val isCurrent = currentMatch?.segmentIndex == 100 + kwIndex

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFF2D2D3A))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                if (kwMatches.isEmpty()) {
                                    append(keyword)
                                } else {
                                    val match = kwMatches.first()
                                    val globalIdx = summaryMatches.indexOf(match)
                                    val isThisCurrent = globalIdx == currentMatchIndex

                                    append(keyword.substring(0, match.matchStart))
                                    withStyle(SpanStyle(
                                        color = if (isThisCurrent) Color.Black else Color(0xFF9B7FD4),
                                        background = if (isThisCurrent) Color(0xFFFF9500) else Color.Transparent,
                                        fontWeight = FontWeight.Bold
                                    )) {
                                        append(keyword.substring(match.matchStart, match.matchEnd))
                                    }
                                    append(keyword.substring(match.matchEnd))
                                }
                            },
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// ── 유틸: ms → "00:00" ───────────────────────────────────────────────────────

private fun formatSearchTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}


private fun formatDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("yyyy.MM.dd · a HH:mm", Locale.KOREAN)
    return dateFormat.format(Date(timestamp))
}

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
