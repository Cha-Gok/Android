package com.roro.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward5
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roro.core.ui.theme.ChaGokTheme

/**
 * ChaGokAudioPlayer
 *
 * 녹음 파일 재생을 위한 하단 오디오 플레이어 컴포넌트.
 *
 * @param currentPositionMs  현재 재생 위치 (밀리초)
 * @param durationMs         전체 재생 길이 (밀리초)
 * @param isPlaying          현재 재생 중 여부
 * @param onPlay             재생 버튼 클릭 콜백
 * @param onPause            일시정지 버튼 클릭 콜백
 * @param onSeek             프로그레스 바 탭 → 새 위치(ms) 콜백
 * @param onRewind           5초 뒤로 콜백
 * @param onForward          5초 앞으로 콜백
 * @param modifier           외부에서 주입할 Modifier
 */
@Composable
fun ChaGokAudioPlayer(
    currentPositionMs: Long,
    durationMs: Long,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (durationMs > 0) currentPositionMs.toFloat() / durationMs else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 200),
        label = "audioProgress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A26))
            .padding(vertical = 14.dp),            // ✅ 1. horizontal padding 제거 → SeekBar 양 끝 붙음
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 프로그레스 바 (패딩 없이 전체 폭)
        SeekBar(
            progress = animatedProgress,
            onSeek = { ratio ->
                onSeek((ratio * durationMs).toLong())
            }
        )

        // 시간 표시 행 (트랙 아래)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(currentPositionMs),
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = formatTime(durationMs),
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )
        }

        // 컨트롤 버튼 행
        Row(
            modifier = Modifier
                .fillMaxWidth(),
                //.padding(top = 2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 5초 뒤로
            IconButton(
                onClick = onRewind,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Replay5,
                    contentDescription = "5초 뒤로",
                    tint = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(28.dp))

            // 재생 / 일시정지 (Pill 버튼)
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF764ACF))
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            if (isPlaying) onPause() else onPlay()
                        })
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "일시정지" else "재생",
                    tint = Color.White,              // 밝은 배경이므로 아이콘은 검정
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(28.dp))

            // 5초 앞으로
            IconButton(
                onClick = onForward,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Forward5,
                    contentDescription = "5초 앞으로",
                    tint = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// ── 내부 컴포넌트: SeekBar ──────────────────────────────────────────────────

@Composable
private fun SeekBar(
    progress: Float,
    onSeek: (Float) -> Unit
) {
    var trackWidthPx by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)                           // 터치 영역 확보
            .onGloballyPositioned { trackWidthPx = it.size.width.toFloat() }
            .pointerInput(trackWidthPx) {
                detectTapGestures(onTap = { offset ->
                    if (trackWidthPx > 0f) {
                        val ratio = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                        onSeek(ratio)
                    }
                })
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // 트랙 배경 (clip 없이 fillMaxWidth → 양 끝 화면 끝까지 붙음)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Color.White.copy(alpha = 0.15f)) // ✅ 1. clip 제거
        )

        // 재생된 구간 (썸 제거 ✅ 3.)
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                .height(6.dp)
                .background(Color(0xFF764ACF))
        )
    }
}

// ── 프리뷰 ─────────────────────────────────────────────────────────────────

/** 재생 중 상태 */
@Preview(showBackground = true, backgroundColor = 0xFF121218, widthDp = 390)
@Composable
private fun ChaGokAudioPlayerPlayingPreview() {
    ChaGokTheme {
        ChaGokAudioPlayer(
            currentPositionMs = 23_000L,
            durationMs        = 4_950_000L,
            isPlaying         = true,
            onPlay    = {},
            onPause   = {},
            onSeek    = {},
            onRewind  = {},
            onForward = {}
        )
    }
}

/** 일시정지 상태 */
@Preview(showBackground = true, backgroundColor = 0xFF121218, widthDp = 390)
@Composable
private fun ChaGokAudioPlayerPausedPreview() {
    ChaGokTheme {
        ChaGokAudioPlayer(
            currentPositionMs = 1_830_000L,
            durationMs        = 4_950_000L,
            isPlaying         = false,
            onPlay    = {},
            onPause   = {},
            onSeek    = {},
            onRewind  = {},
            onForward = {}
        )
    }
}

/** 시작 전 (0:00) 상태 */
@Preview(showBackground = true, backgroundColor = 0xFF121218, widthDp = 390)
@Composable
private fun ChaGokAudioPlayerIdlePreview() {
    ChaGokTheme {
        ChaGokAudioPlayer(
            currentPositionMs = 0L,
            durationMs        = 4_950_000L,
            isPlaying         = false,
            onPlay    = {},
            onPause   = {},
            onSeek    = {},
            onRewind  = {},
            onForward = {}
        )
    }
}

// ── 유틸: 밀리초 → 시:분:초 문자열 ────────────────────────────────────────

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}