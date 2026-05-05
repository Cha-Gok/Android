package com.roro.recorder.presentation.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
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
import com.roro.core.ui.component.ChagokDialog
import com.roro.core.ui.component.RecordingBackground  // ← 추가
import com.roro.recorder.presentation.RecordViewModel
import kotlinx.coroutines.delay
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecorderBottomSheet(
    navController: NavController,
    onDismiss: () -> Unit,
    viewModel: RecordViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0) },
    ) {
        RecorderBottomSheetContent(
            navController = navController,
            viewModel = viewModel,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun RecorderBottomSheetContent(
    navController: NavController,
    viewModel: RecordViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // amplitude → isMaxState 변환
    // 조용할 때 ~50, 말할 때 ~300~2000, 크게 말할 때 ~2000+
    val amplitude by viewModel.amplitude.collectAsStateWithLifecycle()
    val amplitudeNormalized = (amplitude / 2000f).coerceIn(0f, 1f)

    // 임시 로그
//    LaunchedEffect(amplitude) {
//        Timber.d("🎤 amplitude: $amplitude / normalized: $amplitudeNormalized")
//    }

    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(true) }
    var showStopDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    // 타이머
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    // 녹음 완료 → ResultScreen으로 이동
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { voiceNoteId ->
            onDismiss()
            navController.navigate(Routes.recordResult(voiceNoteId))
        }
    }

    // 화면 진입 시 녹음 시작
    LaunchedEffect(Unit) {
        viewModel.startRecording()
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
            .fillMaxWidth()
            .height(680.dp)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
    ) {
        // 기존 Canvas 배경 → RecordingBackground로 교체
        RecordingBackground(
            amplitude = amplitudeNormalized,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
        ) {
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
                            onDismiss()
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
        ChagokDialog(
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
        ChagokDialog(
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
                onDismiss()
            }
        )
    }
}