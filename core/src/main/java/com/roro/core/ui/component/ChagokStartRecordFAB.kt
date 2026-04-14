package com.roro.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// 홈화면 우측 하단 FAB
// 누르면 화면 녹음 화면으로 이동
@Composable
fun ChagokStartRecordFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .size(64.dp)
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .clip(shape)
           // .border(width = 1.dp, color = Color(0xFFDCCCFF), shape = shape)
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0xFFB591FF), // purple800
                        1.00f to Color(0xFF764ACF)  // purple600 0xFF764ACF
                    )
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = "녹음 시작",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Preview(showBackground = false)
@Composable
fun ChagokStartRecordButtonPreview() {
    ChagokStartRecordFAB(onClick = {})
}