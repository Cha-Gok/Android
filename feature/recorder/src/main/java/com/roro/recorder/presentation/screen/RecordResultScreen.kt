package com.roro.recorder.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.roro.core.ui.theme.ChaGokTheme
import com.roro.recorder.domain.usecase.VoiceNoteResult
import com.roro.recorder.presentation.viewModel.RecordResultUiState
import com.roro.recorder.presentation.viewModel.RecordResultViewModel


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
            result = state.result
        )
    }
}

@Composable
private fun RecordResultContent(
    navController: NavController,
    result: VoiceNoteResult
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDropdown by remember { mutableStateOf(false) }
    val tabs = listOf("AI 요약", "키워드", "스크립트")

    // 수정 - * 기준으로 파싱
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
            Text(
                text = "새파일",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { }) {
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
                        onClick = { showDropdown = false }
                    )
                    DropdownMenuItem(
                        text = { Text("삭제하기", color = Color.Red) },
                        onClick = { showDropdown = false }
                    )
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

        // 탭 콘텐츠
        when (selectedTab) {
            0 -> AiSummaryTab(result = result, keyPoints = keyPoints)
            1 -> KeywordTab(keywords = result.keywords)
            2 -> ScriptTab(sttText = result.sttText)
        }
    }
}

@Composable
private fun AiSummaryTab(
    result: VoiceNoteResult,
    keyPoints: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 폴더 + 날짜
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "기본폴더", // TODO: 폴더명 연결
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp
            )
            Text(
                text = "2026.03.02 · 오후 14:32",
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
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .clickable { }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "재생성",
                        tint = Color(0xFF9B7FD4),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(text = "재생성", color = Color(0xFF9B7FD4), fontSize = 13.sp)
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

        // 스크립트 미리보기
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "스크립트",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = result.sttText.take(200),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun KeywordTab(keywords: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "키워드",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        keywords.forEach { keyword ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E1E2E))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "#", color = Color(0xFF9B7FD4), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = keyword, color = Color.White, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun ScriptTab(sttText: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "00:00",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 13.sp
        )
        Text(
            text = sttText,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 15.sp,
            lineHeight = 24.sp
        )
    }
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

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun RecordResultScreenPreview() {
    ChaGokTheme {
        RecordResultContent(
            navController = rememberNavController(),
            result = VoiceNoteResult(
                title = "오전 취업 관련 강의",
                sttText = "채용 시장에서는 학력보다는 실제 역량과 성과를 중심으로 인재를 평가하는 경향이 더 강해지고 있어요.\n\n오늘 UX UI 채용이 이렇게 하기보다는 좀 포트폴리오나 실제 결과물을 훨씬 더 중시하고 있습니다.",
                summaryText = "실무 역량 중심으로 채용 기준이 이동하는 추세\n피그마 링크보다 실제 작업물과 근거가 중요\n이해관계자 조율, 문서화 능력을 기업이 중시",
                keywords = listOf("AI요약", "녹음기능", "즐겨리요약", "수업내용", "최대댓글자키워드", "언제인지", "더보기")
            )
        )
    }
}