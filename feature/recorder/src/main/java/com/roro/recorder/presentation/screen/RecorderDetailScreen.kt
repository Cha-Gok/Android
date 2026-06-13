package com.roro.recorder.presentation.screen

import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.roro.core.navigation.Routes
import com.roro.core.ui.component.ChaGokDialog
import com.roro.recorder.presentation.RecordViewModel
import com.roro.recorder.presentation.uiState.RecordState
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.animation.core.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.roro.core.ui.component.RecordingBackground


// 녹음 화면
@Composable
fun RecorderDetailScreen(
    navController: NavController,
    viewModel: RecordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val amplitude by viewModel.amplitude.collectAsStateWithLifecycle()
    val amplitudeNormalized = (amplitude / 2000f).coerceIn(0f, 1f)

    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(true) }
    var showStopDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var isNavigating by remember { mutableStateOf(false) }

    // 타이머
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    // 처리 완료 → 이동
    LaunchedEffect(Unit) {
        viewModel.navigateToResult.collect {
            isNavigating = true
            navController.navigate(Routes.RECORD_RESULT_WAITING) {
                popUpTo(Routes.RECORDER) { inclusive = true }  // 녹음 화면 백스택에서 제거
            }
        }
    }
    // 화면 진입 시 녹음 시작
    LaunchedEffect(Unit) {
        viewModel.startRecording()
    }

    // 로딩 / 네비게이팅
    if (state == RecordState.Processing || isNavigating) {
        RecordingLoadingScreen()
        return
    }

    // 에러
    if (state is RecordState.Error) {
        RecordingErrorScreen(
            message = (state as RecordState.Error).message,
            onRetry = { viewModel.retry() },
            onBack = { navController.popBackStack() }
        )
        return
    }

    val hours = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60
    val timerText = "%02d : %02d : %02d".format(hours, minutes, seconds)

    val now = remember {
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd · a hh:mm"))
    }

    // ── UI ──
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
    ) {
        RecordingBackground(
            amplitude = amplitudeNormalized,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
        ) {
            // 상단 취소 / 종료
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "취소",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable {
                        if (elapsedSeconds < 3) {
                            navController.popBackStack()
                        } else {
                            isRunning = false
                            showCancelDialog = true
                        }
                    }
                )
                val canStop = elapsedSeconds >= 3
                Text(
                    text = "종료",
                    color = if (canStop) Color(0xFF9B7FD4) else Color(0xFF9B7FD4).copy(alpha = 0.3f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable(enabled = canStop) {
                        isRunning = false
                        showStopDialog = true
                    }
                )
            }

            // 중앙 제목 + 날짜 + 타이머
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "새 기록", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = now, color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(28.dp))
                Text(
                    text = timerText,
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                )
            }

            // 하단 일시정지/재시작 버튼
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 48.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF9B7FD4).copy(alpha = 0.6f))
                    .clickable {
                        isRunning = !isRunning
                        if (isRunning) viewModel.resumeRecording()
                        else viewModel.pauseRecording()
                    }
                    .padding(horizontal = 36.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "일시정지" else "재시작",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    // 종료 다이얼로그
    if (showStopDialog) {
        ChaGokDialog(
            title = "녹음을 종료하고 저장할까요?",
            description = "지금까지 녹음한 내용이 기록됩니다.",
            dismissText = "아니오",
            confirmText = "저장 후 종료",
            onDismiss = {
                showStopDialog = false
                isRunning = true
            },
            onConfirm = {
                showStopDialog = false
                viewModel.stopRecording()
            }
        )
    }

    // 취소 다이얼로그
    if (showCancelDialog) {
        ChaGokDialog(
            title = "녹음을 취소할까요?",
            description = "지금까지 녹음한 내용은 저장되지 않아요",
            dismissText = "계속 녹음",
            confirmText = "녹음 취소",
            confirmColor = Color(0xFFE53935),
            onDismiss = {
                showCancelDialog = false
                isRunning = true
            },
            onConfirm = {
                showCancelDialog = false
                navController.popBackStack()
            }
        )
    }
}

@Composable
private fun RecordingErrorScreen(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121218))
    ) {
        // TopBar - 뒤로가기만 활성화
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "뒤로가기",
                    tint = Color.White
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF2A2A3A))
            )
            Spacer(modifier = Modifier.width(56.dp))
        }

        // TabRow 스켈레톤
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
                        .background(Color(0xFF2A2A3A))
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF2A2A3A))
        )

        // 에러 배너 (보라 → 빨간색)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2E1A1A))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFFF4444),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "오류가 발생했어요. 다시 시도해주세요.",
                color = Color(0xFFFF4444),
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
        }

        // 콘텐츠 영역 - 정적 스켈레톤 (shimmer 없이)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(80, 160, 60).forEach { width ->
                    Box(
                        modifier = Modifier
                            .width(width.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF2A2A3A))
                    )
                }
            }

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
                        .background(Color(0xFF2A2A3A))
                )
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color(0xFF2A2A3A))
                )
            }

            repeat(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1E2E))
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF2A2A3A))
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(56, 72, 48, 64).forEach { width ->
                        Box(
                            modifier = Modifier
                                .width(width.dp)
                                .height(28.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color(0xFF2A2A3A))
                        )
                    }
                }
            }
        }

        // 하단 재시도 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A26))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF9B7FD4))
                    .clickable { onRetry() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "다시 시도",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


@Composable
private fun RecordingLoadingScreen() {
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
        // ── TopBar ──
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
            Spacer(modifier = Modifier.width(8.dp))
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

        // ── TabRow ──
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

        // ── "요약 생성 중..." 배너 ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1A2E))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = Color(0xFF9B7FD4),
                strokeWidth = 2.dp
            )
            Text(
                text = "요약을 생성하고 있어요...",
                color = Color(0xFF9B7FD4),
                fontSize = 13.sp
            )
        }

        // ── 콘텐츠 ──
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 폴더 / 날짜 / 길이
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(80, 160, 60).forEach { width ->
                    Box(
                        modifier = Modifier
                            .width(width.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                }
            }

            // 핵심 포인트 헤더
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

            // 키워드
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

        // ── 하단 오디오 플레이어 ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A26))
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(brush)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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