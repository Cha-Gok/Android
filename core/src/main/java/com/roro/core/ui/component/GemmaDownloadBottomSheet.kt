package com.roro.core.ui.component

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.roro.core.gemma.DownloadBottomSheetState
import com.roro.core.gemma.GemmaDownloadBottomSheetViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GemmaDownloadBottomSheet(
    onDismiss: () -> Unit,
    onDownloadComplete: () -> Unit,
    viewModel: GemmaDownloadBottomSheetViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = {
            // ✅ 다운로드 중이면 내리기 막기
            state !is DownloadBottomSheetState.Downloading
        }
    )
    val scope = rememberCoroutineScope()

    if (state is DownloadBottomSheetState.Completed) {
        onDownloadComplete()
        return
    }

    LaunchedEffect(Unit) {
        Timber.tag("DownloadBS").d("🪟 바텀시트 열림, 현재 state: ${viewModel.state.value}")
    }

    ModalBottomSheet(
        onDismissRequest = {
            // 다운로드 중이면 무시
            if (state !is DownloadBottomSheetState.Downloading) {
                viewModel.resetState()
                onDismiss()
            }
        },
        sheetState = sheetState,
        containerColor = Color(0xFF1A1A26),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "음성을 글로 옮길 준비가 필요해요",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "녹음과 요약을 모두 기기 안에서\n처리하기 위해 AI 모델이 필요해요",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }

                when (val s = state) {
                    is DownloadBottomSheetState.Idle -> IdleContent()
                    is DownloadBottomSheetState.Downloading -> DownloadingContent(progress = s.progress)
                    is DownloadBottomSheetState.NetworkError -> ErrorContent(message = "네트워크 연결을 확인해주세요.")
                    is DownloadBottomSheetState.UnknownError -> ErrorContent(message = s.message)
                    else -> {}
                }

                Text(
                    text = "Wi-Fi 연결을 권장하며 몇 분 정도 걸려요.",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 13.sp
                )

                when (state) {
                    is DownloadBottomSheetState.Idle -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        sheetState.hide()  // ← 애니메이션 내려가게 하기
                                    }.invokeOnCompletion {
                                        viewModel.resetState()
                                        onDismiss()        // ← 완료 후 닫기
                                    }
                                },
                                modifier = Modifier.weight(1f).height(52.dp),
                                shape = RoundedCornerShape(50.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("나중에", fontSize = 16.sp)
                            }
                            Button(
                                onClick = { viewModel.startDownload() },
                                modifier = Modifier.weight(1f).height(52.dp),
                                shape = RoundedCornerShape(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B7FD4))
                            ) {
                                Text("다운로드", fontSize = 16.sp, color = Color.White)
                            }
                        }
                    }
                    is DownloadBottomSheetState.NetworkError,
                    is DownloadBottomSheetState.UnknownError -> {
                        Button(
                            onClick = { viewModel.startDownload() },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B7FD4))
                        ) {
                            Text("다시 시도", fontSize = 16.sp, color = Color.White)
                        }
                    }
                    else -> {
                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = Color(0xFF2A2A3A)
                            )
                        ) {
                            Text("다운로드 중...", fontSize = 16.sp, color = Color.White.copy(alpha = 0.4f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IdleContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF12121F), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf(
            "녹음한 목소리가 기기 밖으로 나가지 않아요",
            "인터넷 없이도 받아쓰기와 요약이 가능해요",
            "길이 제한 없이 마음껏 기록할 수 있어요"
        ).forEach { text ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = Color(0xFF9B7FD4),
                    modifier = Modifier.size(16.dp)
                )
                Text(text = text, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun DownloadingContent(progress: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF12121F), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
                Text(text = "Gemma-4", color = Color.White, fontSize = 14.sp)
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                color = Color(0xFF9B7FD4),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = Color(0xFF9B7FD4),
            trackColor = Color.White.copy(alpha = 0.1f)
        )
        Text(
            text = "다운로드 중입니다...",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ErrorContent(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF12121F), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Text(text = "Gemma-4", color = Color.White, fontSize = 14.sp)
        }
        LinearProgressIndicator(
            progress = { 0f },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = Color(0xFFE53935),
            trackColor = Color.White.copy(alpha = 0.1f)
        )
        Text(
            text = message,
            color = Color(0xFFE53935),
            fontSize = 13.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun GemmaDownloadBottomSheetIdlePreview() {
    ModalBottomSheet(
        onDismissRequest = {},
        containerColor = Color(0xFF1A1A26),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(380.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "음성을 글로 옮길 준비가 필요해요", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "녹음과 요약을 모두 기기 안에서\n처리하기 위해 AI 모델이 필요해요", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, lineHeight = 20.sp)
                }
                IdleContent()
                Text(text = "Wi-Fi 연결을 권장하며 몇 분 정도 걸려요.", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = {}, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(50.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) {
                        Text("나중에", fontSize = 16.sp)
                    }
                    Button(onClick = {}, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B7FD4))) {
                        Text("다운로드", fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun GemmaDownloadBottomSheetDownloadingPreview() {
    ModalBottomSheet(
        onDismissRequest = {},
        containerColor = Color(0xFF1A1A26),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(380.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "음성을 글로 옮길 준비가 필요해요", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "녹음과 요약을 모두 기기 안에서\n처리하기 위해 AI 모델이 필요해요", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, lineHeight = 20.sp)
                }
                DownloadingContent(progress = 0.4f)
                Text(text = "Wi-Fi 연결을 권장하며 몇 분 정도 걸려요.", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
                Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(50.dp), colors = ButtonDefaults.buttonColors(disabledContainerColor = Color(0xFF2A2A3A))) {
                    Text("다운로드 중...", fontSize = 16.sp, color = Color.White.copy(alpha = 0.4f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun GemmaDownloadBottomSheetErrorPreview() {
    ModalBottomSheet(
        onDismissRequest = {},
        containerColor = Color(0xFF1A1A26),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "음성을 글로 옮길 준비가 필요해요", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(text = "녹음과 요약을 모두 기기 안에서\n처리하기 위해 AI 모델이 필요해요", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp, lineHeight = 20.sp)
                }
                ErrorContent(message = "다운로드에 실패했습니다.")
                Text(text = "Wi-Fi 연결을 권장하며 몇 분 정도 걸려요.", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
                Button(onClick = {}, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9B7FD4))) {
                    Text("다시 시도", fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}