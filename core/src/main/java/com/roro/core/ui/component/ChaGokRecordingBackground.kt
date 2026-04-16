package com.roro.core.ui.component

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.util.lerp
import androidx.room.util.copy
import androidx.compose.ui.graphics.lerp as lerpColor

@Composable
fun RecordingBackground(
    amplitude: Float, // 0f ~ 1f
    modifier: Modifier = Modifier
) {
    val bottomRadius by animateFloatAsState(
        targetValue = lerp(400f, 700f, amplitude),
        animationSpec = tween(300),
        label = "bottomRadius"
    )
    val topRadius by animateFloatAsState(
        targetValue = lerp(200f, 400f, amplitude),
        animationSpec = tween(300),
        label = "topRadius"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val bottomPaint = Paint().apply {
            asFrameworkPaint().apply {
                isAntiAlias = true
                color = android.graphics.Color.TRANSPARENT
                setShadowLayer(0f, 0f, 0f, android.graphics.Color.TRANSPARENT)
                maskFilter = BlurMaskFilter(bottomRadius * 0.6f, BlurMaskFilter.Blur.NORMAL)
            }
        }
        val topPaint = Paint().apply {
            asFrameworkPaint().apply {
                isAntiAlias = true
                color = android.graphics.Color.TRANSPARENT
                maskFilter = BlurMaskFilter(topRadius * 0.5f, BlurMaskFilter.Blur.NORMAL)
            }
        }

        // Ellipse 14 - 아래 큰 빛
        drawContext.canvas.drawCircle(
            center = Offset(size.width / 2f, size.height),
            radius = bottomRadius,
            paint = bottomPaint.apply {
                asFrameworkPaint().color = lerpColor(
                    Color(0xFF7B4FCC), // purple500
                    Color(0xFF6B3FBC), // purple600
                    amplitude
                ).copy(alpha = lerp(0.3f, 0.7f, amplitude)).toArgb()
            }
        )

        // Ellipse 13 - 위 작은 빛
        drawContext.canvas.drawCircle(
            center = Offset(size.width / 2f, size.height * 0.3f),
            radius = topRadius,
            paint = topPaint.apply {
                asFrameworkPaint().color = lerpColor(
                    Color(0xFF9B7FD4), // purple300
                    Color(0xFF7B4FCC), // purple500
                    amplitude
                ).copy(alpha = lerp(0.2f, 0.5f, amplitude)).toArgb()
            }
        )
    }
}