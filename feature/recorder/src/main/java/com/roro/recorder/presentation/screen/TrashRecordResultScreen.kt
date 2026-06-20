package com.roro.recorder.presentation.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.roro.core.domain.model.SummaryStatus
import com.roro.recorder.domain.usecase.VoiceNoteResult
import com.roro.recorder.presentation.viewModel.RecordResultUiState
import com.roro.recorder.presentation.viewModel.RecordResultViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TrashRecordResultScreen(
    navController: NavController,
    voiceNoteId: String,
    viewModel: RecordResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(voiceNoteId) {
        if (voiceNoteId.isNotEmpty()) viewModel.load(voiceNoteId)
    }

    when (val state = uiState) {
        is RecordResultUiState.Loading -> TrashLoadingScreen()
        is RecordResultUiState.Error -> TrashErrorScreen(message = state.message)
        is RecordResultUiState.Success -> TrashRecordResultContent(
            navController = navController,
            result = state.result
        )
        is RecordResultUiState.NoSpeech -> TrashRecordResultContent(
            navController = navController,
            result = state.result
        )
        is RecordResultUiState.SummaryError -> TrashRecordResultContent(
            navController = navController,
            result = state.result
        )
        is RecordResultUiState.SummaryGenerating -> TrashRecordResultContent(
            navController = navController,
            result = state.result
        )
    }
}

@Composable
private fun TrashRecordResultContent(
    navController: NavController,
    result: VoiceNoteResult
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("요약", "스크립트")

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
        // ── TopBar: 뒤로가기 + 제목(읽기 전용) ──────────────────────────────
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
                text = result.title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        // ── 탭 ───────────────────────────────────────────────────────────
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

        // ── 탭 콘텐츠 ────────────────────────────────────────────────────
        when (selectedTab) {
            0 -> TrashAiSummaryTab(result = result, keyPoints = keyPoints)
            1 -> TrashScriptTab(sttText = result.sttText)
        }
    }
}

// ── 요약 탭 ──────────────────────────────────────────────────────────────────

@Composable
private fun TrashAiSummaryTab(
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
            Text(text = dateText, color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
            Text(text = formatDuration(result.durationSec), color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
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
                // ✅ 재생성 버튼 - 비활성화(dimmed), clickable 없음
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color(0xFF9B7FD4).copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "재생성",
                        color = Color(0xFF9B7FD4).copy(alpha = 0.4f),
                        fontSize = 13.sp
                    )
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
            Text(text = "키워드", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
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




// ── 스크립트 탭: 읽기 전용 ────────────────────────────────────────────────────

@Composable
private fun TrashScriptTab(sttText: String) {
    val segments = remember(sttText) {
        sttText.split("\n")
            .mapIndexed { index, text -> Pair(index * 7000L, text) }
            .filter { it.second.isNotBlank() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        segments.forEach { (startMs, text) ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = formatTime(startMs),
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
                Text(
                    text = text,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 15.sp,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

// ── 로딩 (스켈레톤 UI)─────────────────────────────────────────────────────────────────────

@Composable
private fun TrashLoadingScreen() {
    val shimmerColors = listOf(
        Color(0xFF2A2A3A),
        Color(0xFF3A3A4E),
        Color(0xFF2A2A3A)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 300f, 0f),
        end = Offset(translateAnim, 0f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121218))
    ) {
        // ── TopBar: 뒤로가기 + 제목만 (검색/더보기 없음) ─────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(brush)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }

        // ── TabRow ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
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

        // ── 콘텐츠 ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()   // weight(1f) → fillMaxSize (플레이어 제거)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(brush)
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
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
    }
}

// ── 에러 ─────────────────────────────────────────────────────────────────────
// 오류 발생 화면 -> 수정 예정
@Composable
private fun TrashErrorScreen(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121218)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = Color.White, fontSize = 14.sp)
    }
}

// ── 유틸 ─────────────────────────────────────────────────────────────────────

private fun formatTime(ms: Long): String {
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

// ── 프리뷰 ───────────────────────────────────────────────────────────────────

private val fakeVoiceNoteResult = VoiceNoteResult(
    title = "오전 취업 관련 강의",
    sttText = "오늘은 취업 관련 강의를 들었습니다.\n면접 준비 방법에 대해 배웠습니다.\n자기소개서 작성 팁도 공유되었습니다.",
    summaryText = "취업 준비의 핵심은 자기분석이다*면접에서는 구체적인 경험을 말해야 한다*자기소개서는 직무 중심으로 작성해야 한다",
    keywords = listOf("취업", "면접", "자기소개서", "직무"),
    audioPath = "",
    createdAt = System.currentTimeMillis(),
    updatedAt = System.currentTimeMillis(),
    durationSec = 4350.0,
    summaryStatus = SummaryStatus.SUCCESS,
)

@Preview(showBackground = true)
@Composable
private fun TrashLoadingPreview() {
    TrashLoadingScreen()
}

@Preview(showBackground = true)
@Composable
private fun TrashErrorPreview() {
    TrashErrorScreen(message = "오류가 발생했어요. 다시 시도해주세요.")
}

@Preview(showBackground = true)
@Composable
private fun TrashRecordResultContentPreview() {
    TrashRecordResultContent(
        navController = rememberNavController(),
        result = fakeVoiceNoteResult
    )
}

