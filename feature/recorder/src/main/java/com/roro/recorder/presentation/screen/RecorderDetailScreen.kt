package com.roro.recorder.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import com.roro.core.ui.component.ChaGokBackground
import com.roro.recorder.presentation.RecordViewModel
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

    // 타이머
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(true) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            elapsedSeconds++
        }
    }

    val hours = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60
    val timerText = "%02d : %02d : %02d".format(hours, minutes, seconds)

    val now = remember {
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd · a hh:mm"))
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
                        viewModel.stopRecording()
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

    // 화면 진입 시 녹음 시작
    LaunchedEffect(Unit) {
        viewModel.startRecording()
    }
}