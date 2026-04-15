package com.roro.recorder.presentation.screen

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
import com.roro.core.ui.component.ChaGokBackground
import com.roro.core.ui.component.ChagokDialog
import com.roro.recorder.presentation.RecordViewModel
import com.roro.recorder.presentation.uiState.RecordState
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun RecorderDetailScreen(
    navController: NavController,
    fileId: String,
    viewModel: RecordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(true) }
    var showStopDialog by remember { mutableStateOf(false) }


    // 타이머
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    // 녹음 완료 → ResultScreen 이동
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { voiceNoteId ->
            navController.navigate(Routes.recordResult(voiceNoteId)) {
                popUpTo(Routes.RECORD_DETAIL) { inclusive = true }
            }
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

    // 로딩 중이면 로딩 화면
    if (state == RecordState.Processing) {
        RecordingLoadingScreen()
        return
    }

    ChaGokBackground {
        Box(modifier = Modifier.fillMaxSize()) {

            // 상단 취소 / 종료
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "취소",
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable {
                        navController.popBackStack()
                    }
                )
                Text(
                    text = "종료",
                    color = Color(0xFF9B7FD4),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        isRunning = false
                        showStopDialog = true
                        //viewModel.stopRecording()
                        //navController.popBackStack()
                    }
                )
            }

            // 중앙 제목 + 날짜 + 타이머
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "새 기록",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = now,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = timerText,
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.sp
                )
            }

            // 하단 일시정지/재시작 버튼
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 60.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF9B7FD4).copy(alpha = 0.6f))
                    .clickable { isRunning = !isRunning }
                    .padding(horizontal = 36.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    // Icons.Default.Mic
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
}

@Composable
private fun RecordingLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = Color(0xFF9B7FD4))
            Text(text = "기록 요약이 진행중입니다...", color = Color.White, fontSize = 16.sp)
        }
    }
}