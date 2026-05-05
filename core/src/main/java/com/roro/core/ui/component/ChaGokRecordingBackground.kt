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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.util.lerp
import androidx.compose.ui.graphics.lerp as lerpColor

/**
 * 녹음 화면 배경
 * SVG 스펙 기준 (390x844):
 *
 * [Ellipse - 큰 타원]
 *   최솟값: cx=195, cy=815.5, rx=211, ry=97.5,  blur=100, color=#250062
 *   최댓값: cx=195, cy=591.5, rx=211, ry=321.5, blur=250, color=#5724AA
 *
 * [Path - 하단 호]
 *   최솟값: blur=40,  color=#5724AA
 *   최댓값: blur=100, color=#754ACF
 *
 * @param amplitude 0f(최솟값) ~ 1f(최댓값)
 */
@Composable
fun RecordingBackground(
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    // Ellipse cy: 815.5 → 591.5
    val ellipseCy by animateFloatAsState(
        targetValue = lerp(815.5f, 591.5f, amplitude),
        animationSpec = tween(300),
        label = "ellipseCy"
    )
    // Ellipse ry: 97.5 → 321.5
    val ellipseRy by animateFloatAsState(
        targetValue = lerp(97.5f, 321.5f, amplitude),
        animationSpec = tween(300),
        label = "ellipseRy"
    )
    // Ellipse blur: 100 → 250
    val ellipseBlur by animateFloatAsState(
        targetValue = lerp(100f, 250f, amplitude),
        animationSpec = tween(300),
        label = "ellipseBlur"
    )
    // Path blur: 40 → 100
    val pathBlur by animateFloatAsState(
        targetValue = lerp(40f, 100f, amplitude),
        animationSpec = tween(300),
        label = "pathBlur"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val scaleX = size.width / 390f
        val scaleY = size.height / 844f

        // 배경
        drawRect(color = Color(0xFF121212))

        // ── Ellipse (큰 타원 빛) ──────────────────────────────
        val ellipseColor = lerpColor(
            Color(0xFF250062),
            Color(0xFF5724AA),
            amplitude
        )

        val ellipsePaint = Paint().apply {
            asFrameworkPaint().apply {
                isAntiAlias = true
                color = ellipseColor.toArgb()
                maskFilter = BlurMaskFilter(
                    ellipseBlur * scaleY,
                    BlurMaskFilter.Blur.NORMAL
                )
            }
        }

        val cx = 195f * scaleX
        val cy = ellipseCy * scaleY
        val rx = 211f * scaleX
        val ry = ellipseRy * scaleY

        drawContext.canvas.drawOval(
            left = cx - rx,
            top = cy - ry,
            right = cx + rx,
            bottom = cy + ry,
            paint = ellipsePaint
        )

        // ── Path (하단 호형 빛) ───────────────────────────────
        val pathColor = lerpColor(
            Color(0xFF5724AA),
            Color(0xFF754ACF),
            amplitude
        )

        val pathPaint = Paint().apply {
            asFrameworkPaint().apply {
                isAntiAlias = true
                color = pathColor.toArgb()
                maskFilter = BlurMaskFilter(
                    pathBlur * scaleY,
                    BlurMaskFilter.Blur.NORMAL
                )
            }
        }

        // SVG path의 호형 → 타원 하단부로 근사
        // 최솟값 시작y=886, 최댓값 시작y=844 (화면 하단 기준)
        val pathStartY = lerp(886f, 844f, amplitude) * scaleY
        val pathRx = 192f * scaleX  // (387-3)/2
        val pathRy = (pathStartY - size.height) * -1f

        drawContext.canvas.drawOval(
            left = cx - pathRx,
            top = pathStartY - pathRy * 2,
            right = cx + pathRx,
            bottom = pathStartY,
            paint = pathPaint
        )
    }
}