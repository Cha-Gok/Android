package com.roro.recorder.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.roro.recorder.presentation.viewModel.ScriptEditUiState
import com.roro.recorder.presentation.viewModel.ScriptEditViewModel
import kotlinx.coroutines.launch

/**
 * 기능 설명:
 * - 스크립트(STT 텍스트) 세그먼트별 직접 편집 화면
 * - DB에서 직접 sttText 조회
 * - 완료 시 스낵바 표시 후 RecordResultScreen으로 복귀
 *
 * @author
 * @since 2026. 04. 19.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptEditScreen(
    navController: NavController,
    voiceNoteId: String,
    onScriptSaved: () -> Unit,
    viewModel: ScriptEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val segments by viewModel.segments.collectAsStateWithLifecycle()
    val focusedIndex by viewModel.focusedIndex.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // ✅ voiceNoteId로 DB에서 직접 로드
    LaunchedEffect(voiceNoteId) {
        viewModel.load(voiceNoteId)
    }

    // 저장 완료 → 스낵바 → 복귀
    LaunchedEffect(uiState) {
        if (uiState is ScriptEditUiState.Saved) {
            if (viewModel.isModified) {
                onScriptSaved()
                scope.launch {
                    snackbarHostState.showSnackbar("스크립트가 수정됐어요")
                }
            }
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = Color(0xFF121218),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF2D2D3A),
                    contentColor = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // TopBar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "취소",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = "스크립트",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (uiState is ScriptEditUiState.Saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF9B7FD4),
                        strokeWidth = 2.dp
                    )
                } else {
                    TextButton(onClick = { viewModel.save(voiceNoteId) }) {
                        Text(
                            text = "완료",
                            color = Color(0xFF9B7FD4),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            // 로딩 중
            if (uiState is ScriptEditUiState.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF9B7FD4))
                }
                return@Scaffold
            }

            // 세그먼트 목록
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                itemsIndexed(segments) { index, segment ->
                    val isFocused = focusedIndex == index

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isFocused) Color(0xFF1E1E2E) else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(if (isFocused) 10.dp else 0.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 타임스탬프
                        Text(
                            text = formatSegmentTime(segment.startTimeMs),
                            color = if (isFocused) Color(0xFF9B7FD4) else Color.White.copy(alpha = 0.4f),
                            fontSize = 12.sp,
                            fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal
                        )
                        // 편집 가능한 텍스트
                        BasicTextField(
                            value = segment.text,
                            onValueChange = { viewModel.onSegmentChange(index, it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) viewModel.onSegmentFocused(index)
                                    else viewModel.onSegmentUnfocused()
                                },
                            textStyle = TextStyle(
                                color = if (isFocused) Color.White else Color.White.copy(alpha = 0.75f),
                                fontSize = 15.sp,
                                lineHeight = 24.sp
                            ),
                            cursorBrush = SolidColor(Color(0xFF9B7FD4))
                        )
                    }
                }
            }
        }
    }
}

// ── 유틸: ms → "00:00" ───────────────────────────────────────────────────────

private fun formatSegmentTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}