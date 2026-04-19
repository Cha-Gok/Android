package com.roro.recorder.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.roro.recorder.presentation.viewModel.SearchMatch
import com.roro.recorder.presentation.viewModel.SearchResultViewModel
import kotlinx.coroutines.launch

@Composable
fun SearchResultScreen(
    navController: NavController,
    summaryText: String,
    sttText: String,
    onSeek: (Long) -> Unit = {},   // 스크립트 결과 탭 → 해당 위치로 seek
    viewModel: SearchResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val tabs = listOf("요약", "스크립트")

    LaunchedEffect(Unit) {
        viewModel.init(summaryText, sttText)
        focusRequester.requestFocus()
    }

    // 현재 매칭 인덱스 변경 시 해당 항목으로 스크롤
    val currentMatches = if (uiState.selectedTab == 0)
        uiState.summaryMatches else uiState.scriptMatches

    LaunchedEffect(uiState.currentMatchIndex, uiState.selectedTab) {
        if (currentMatches.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(uiState.currentMatchIndex)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121218))
    ) {
        // ── 검색 TopBar ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "뒤로가기",
                    tint = Color.White
                )
            }

            // 검색 입력창
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF2D2D3A), RoundedCornerShape(50.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BasicTextField(
                    value = uiState.query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 15.sp
                    ),
                    cursorBrush = SolidColor(Color(0xFF9B7FD4)),
                    decorationBox = { innerTextField ->
                        if (uiState.query.isEmpty()) {
                            Text(
                                text = "검색",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 15.sp
                            )
                        }
                        innerTextField()
                    },
                    singleLine = true
                )
                // 지우기 버튼
                if (uiState.query.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "지우기",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { viewModel.onQueryChange("") }
                    )
                }
            }

            // 결과 이동 (검색어 있을 때만)
            if (currentMatches.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${uiState.currentMatchIndex + 1}/${currentMatches.size}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                    IconButton(
                        onClick = viewModel::prevMatch,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "이전",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = viewModel::nextMatch,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "다음",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // ── 탭 (결과 수 포함) ─────────────────────────────────────────────────
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
                                        .background(Color(0xFF9B7FD4), CircleShape)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "$count",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

        // ── 결과 목록 ─────────────────────────────────────────────────────────
        if (uiState.query.isBlank()) {
            // 검색어 없을 때 안내
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "검색어를 입력해주세요",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 14.sp
                )
            }
        } else if (currentMatches.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "검색 결과가 없어요",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(currentMatches) { index, match ->
                    val isCurrent = index == uiState.currentMatchIndex
                    SearchMatchItem(
                        match = match,
                        query = uiState.query,
                        isCurrent = isCurrent,
                        showTimestamp = uiState.selectedTab == 1,
                        onClick = {
                            viewModel.onTabSelected(uiState.selectedTab)
                            if (uiState.selectedTab == 1) onSeek(match.startTimeMs)
                        }
                    )
                }
            }
        }
    }
}

// ── 검색 결과 아이템 ──────────────────────────────────────────────────────────

@Composable
private fun SearchMatchItem(
    match: SearchMatch,
    query: String,
    isCurrent: Boolean,
    showTimestamp: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isCurrent) Color(0xFF1E1E2E) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(if (isCurrent) 12.dp else 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 타임스탬프 (스크립트 탭만)
        if (showTimestamp) {
            Text(
                text = formatSearchTime(match.startTimeMs),
                color = if (isCurrent) Color(0xFF9B7FD4) else Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
            )
        }

        // 하이라이트 텍스트
        Text(
            text = buildAnnotatedString {
                val before = match.text.substring(0, match.matchStart)
                val highlighted = match.text.substring(match.matchStart, match.matchEnd)
                val after = match.text.substring(match.matchEnd)

                append(before)
                withStyle(
                    SpanStyle(
                        color = Color.White,
                        background = Color(0xFF9B7FD4),
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(highlighted)
                }
                append(after)
            },
            color = if (isCurrent) Color.White else Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            lineHeight = 22.sp
        )
    }
}

// ── 유틸: ms → "00:00" ───────────────────────────────────────────────────────

private fun formatSearchTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}