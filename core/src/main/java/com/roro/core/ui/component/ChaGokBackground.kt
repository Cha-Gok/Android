package com.roro.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roro.core.ui.theme.Gray50

@Composable
fun ChaGokBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Gray50) // 기본 배경색 (매우 밝은 회색)
    ) {
        // 1. 뒤쪽의 큰 타원 (더 넓고 은은하게 퍼지는 보라색 광채)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp) // 높이를 충분히 주어 부드럽게 감춤
                .align(Alignment.BottomCenter)
                .offset(y = 150.dp) // 화면 아래로 절반 정도 걸치게 배치
                .graphicsLayer {
                    scaleX = 2.5f // 가로로 아주 넓게 확장
                    scaleY = 1.0f
                    alpha = 0.6f  // 투명도 조절
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF7B3FF2).copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // 2. 앞쪽의 작은 타원 (더 밝고 집중된 하단 광채)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.BottomCenter)
                .offset(y = 120.dp)
                .graphicsLayer {
                    scaleX = 1.8f // 적당한 너비
                    scaleY = 0.8f // 위아래로 약간 납작하게
                }
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF9C6BFF).copy(alpha = 0.7f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        // 실제 컨텐츠 레이어
        content()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ChaGokBackgroundPreview() {
    // 배경 위에 흰색 텍스트나 다른 컴포넌트가 올라간 모습 확인
    ChaGokBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

        }
    }
}