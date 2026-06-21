package com.roro.core.ui.theme

import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.roro.core.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * 기능 설명:
 * - 앱에서 사용하는 기본 텍스트 스타일 정의
 * 일단 차곡앱은 없음...
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_bold, FontWeight.Bold)
)

val ChaGokTypography = Typography(
    displayLarge = ChaGokTextStyle.Header1,
    displayMedium = ChaGokTextStyle.Header2,
    titleLarge = ChaGokTextStyle.Title1,
    bodyLarge = ChaGokTextStyle.Body1,

    headlineMedium = ChaGokTextStyle.Title1  // sp와 em이 혼용 오류 해결용 ← 추가
)

//object ChaGokTextStyle {
//
//    // Headers
//    val Header1 = TextStyle(
//        fontFamily = Pretendard,
//        fontWeight = FontWeight.Medium,
//        fontSize = 28.sp,
//        lineHeight = (28 * 1.3).sp,
//        letterSpacing = 0.em
//    )
//    val Header2 = TextStyle(
//        fontFamily = Pretendard,
//        fontWeight = FontWeight.Bold,
//        fontSize = 24.sp,
//        lineHeight = (24 * 1.3).sp,
//        letterSpacing = (-0.02).em
//    )
//
//    // Titles
//    val Title1 = TextStyle(
//        fontFamily = Pretendard,
//        fontWeight = FontWeight.Bold,
//        fontSize = 20.sp,
//        lineHeight = (20 * 1.3).sp,
//        letterSpacing = (-0.02).em
//    )
//
//    val Title2 = TextStyle(
//        fontFamily = Pretendard,
//        fontWeight = FontWeight.Bold,
//        fontSize = 18.sp,
//        lineHeight = (18 * 1.3).sp,
//        letterSpacing = (-0.02).em
//    )
//
//    val Title3 = TextStyle(
//        fontFamily = Pretendard,
//        fontWeight = FontWeight.Bold,
//        fontSize = 16.sp,
//        lineHeight = (16 * 1.3).sp,
//        letterSpacing = (-0.02).em
//    )
//
//    // Subtitles
//    val Subtitle1 = TextStyle(
//        fontFamily = Pretendard,
//        fontWeight = FontWeight.Medium,
//        fontSize = 18.sp,
//        lineHeight = (18 * 1.5).sp,
//        letterSpacing = 0.em
//    )
//
//    val Subtitle2 = TextStyle(
//        fontFamily = Pretendard,
//        fontWeight = FontWeight.Medium,
//        fontSize = 16.sp,
//        lineHeight = (16 * 1.3).sp,
//        letterSpacing = (-0.03).em
//    )
//
//    // Body
//    val Body1 = TextStyle(
//        fontFamily = Pretendard,
//        fontWeight = FontWeight.Normal,
//        fontSize = 16.sp,
//        lineHeight = (16 * 1.5).sp,
//        letterSpacing = (-0.03).em
//    )
//
//    val Body2 = TextStyle(
//        fontFamily = Pretendard,
//        fontWeight = FontWeight.Normal,
//        fontSize = 16.sp,
//        lineHeight = (16 * 1.3).sp,
//        letterSpacing = (-0.03).em
//    )
//
//    val Body3 = TextStyle(
//        fontFamily = Pretendard,
//        fontWeight = FontWeight.Normal,
//        fontSize = 15.sp,
//        lineHeight = (15 * 1.5).sp,
//        letterSpacing = (-0.03).em
//    )
//
//    // Label / Caption
//    val Label = TextStyle(
//        fontFamily = Pretendard,
//        fontWeight = FontWeight.Normal,
//        fontSize = 15.sp,
//        lineHeight = (15 * 1.3).sp,
//        letterSpacing = (-0.03).em
//    )
//
//    val Caption = TextStyle(
//        fontFamily = Pretendard,
//        fontWeight = FontWeight.Normal,
//        fontSize = 14.sp,
//        lineHeight = (14 * 1.3).sp,
//        letterSpacing = (-0.02).em
//    )
//}

object ChaGokTextStyle {

    // 0414 임시 수정
    // lineHeight와 letterSpacing 모두 em 단위로 통일
    // em = 현재 fontSize 기준 배수 (예: 1.3.em = fontSize * 1.3)
    // sp와 em 혼용 시 IllegalArgumentException 발생하므로 em으로 통일

    // Headers
    val Header1 = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,

        lineHeight = (28 * 1.3).sp,
        letterSpacing = 0.sp
    )
    val Header2 = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = (24 * 1.3).sp,
        letterSpacing = (-0.02).sp
    )

    // Titles
    val Title1 = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = (20 * 1.3).sp,
        letterSpacing = (-0.02).sp
    )
    val Title2 = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = (18 * 1.3).sp,
        letterSpacing = (-0.02).sp
    )
    val Title3 = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = (16 * 1.3).sp,
        letterSpacing = (-0.02).sp
    )

    // Subtitles
    val Subtitle1 = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = (18 * 1.5).sp,
        letterSpacing = 0.sp
    )
    val Subtitle2 = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = (16 * 1.3).sp,
        letterSpacing = (-0.03).sp
    )

    // Body
    val Body1 = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = (16 * 1.5).sp,
        letterSpacing = (-0.03).sp
    )
    val Body2 = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = (16 * 1.3).sp,
        letterSpacing = (-0.03).sp
    )
    val Body3 = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = (15 * 1.5).sp,
        letterSpacing = (-0.03).sp
    )

    // Label / Caption
    val Label = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = (15 * 1.3).sp,
        letterSpacing = (-0.03).sp
    )
    val Caption = TextStyle(
        fontFamily = Pretendard,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = (14 * 1.3).sp,
        letterSpacing = (-0.02).sp
    )
}

@Preview(
    showBackground = true,
   // backgroundColor = 0xFF121212,
    showSystemUi = true
)
@Composable
fun ChaGokTypographyPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Header1", style = ChaGokTextStyle.Header1)
        Text("Header2", style = ChaGokTextStyle.Header2)

        Spacer(modifier = Modifier.height(8.dp))

        Text("Title1", style = ChaGokTextStyle.Title1)
        Text("Title2", style = ChaGokTextStyle.Title2)
        Text("Title3", style = ChaGokTextStyle.Title3)

        Spacer(modifier = Modifier.height(8.dp))

        Text("Subtitle1", style = ChaGokTextStyle.Subtitle1)
        Text("Subtitle2", style = ChaGokTextStyle.Subtitle2)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Body1 - 안녕하세요. 차곡 텍스트 스타일입니다.",
            style = ChaGokTextStyle.Body1
        )

        Text(
            "Body2 - 안녕하세요. 차곡 텍스트 스타일입니다.",
            style = ChaGokTextStyle.Body2
        )

        Text(
            "Body3 - 안녕하세요. 차곡 텍스트 스타일입니다.",
            style = ChaGokTextStyle.Body3
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Label - 설정",
            style = ChaGokTextStyle.Label
        )

        Text(
            "Caption - 마지막 수정: 2026.06.20",
            style = ChaGokTextStyle.Caption
        )
    }
}