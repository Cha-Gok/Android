package com.roro.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import com.roro.core.R

/**
 * 기능 설명:
 * - 앱에서 사용하는 기본 텍스트 스타일 정의
 * 일단 차곡앱은 없음...
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
val pretendard = FontFamily(
    Font(R.font.pretendard_regular),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_bold, FontWeight.Bold)
)

val ChaGokTypography = Typography(

    // Headers
    displayLarge = TextStyle(
        fontFamily = pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = (28 * 1.3).sp,
        letterSpacing = 0.em
    ),
    displayMedium = TextStyle(
        fontFamily = pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = (24 * 1.3).sp,
        letterSpacing = (-0.02).em
    ),

    // Titles
    titleLarge = TextStyle(
        fontFamily = pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = (20 * 1.3).sp,
        letterSpacing = (-0.02).em
    ),
    titleMedium = TextStyle(
        fontFamily = pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = (18 * 1.3).sp,
        letterSpacing = (-0.02).em
    ),
    titleSmall = TextStyle(
        fontFamily = pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = (16 * 1.3).sp,
        letterSpacing = (-0.02).em
    ),

    // Subtitles
    headlineSmall = TextStyle(
        fontFamily = pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = (18 * 1.5).sp,
        letterSpacing = 0.em
    ),
    headlineMedium = TextStyle(
        fontFamily = pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = (16 * 1.3).sp,
        letterSpacing = (-0.03).em
    ),

    // Body
    bodyLarge = TextStyle(
        fontFamily = pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = (16 * 1.5).sp,
        letterSpacing = (-0.03).em
    ),
    bodyMedium = TextStyle(
        fontFamily = pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = (16 * 1.3).sp,
        letterSpacing = (-0.03).em
    ),
    bodySmall = TextStyle(
        fontFamily = pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = (15 * 1.5).sp,
        letterSpacing = (-0.03).em
    ),

    // Label / Caption
    labelLarge = TextStyle(
        fontFamily = pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = (15 * 1.3).sp,
        letterSpacing = (-0.03).em
    ),
    labelSmall = TextStyle(
        fontFamily = pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = (14 * 1.3).sp,
        letterSpacing = (-0.02).em
    )
)