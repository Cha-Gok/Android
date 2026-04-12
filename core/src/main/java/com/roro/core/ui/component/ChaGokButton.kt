package com.roro.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.roro.core.ui.theme.ChaGokTextStyle
import com.roro.core.ui.theme.ChaGokTheme
import com.roro.core.ui.theme.TextDisabled
import com.roro.core.ui.theme.PrimaryColor
import com.roro.core.ui.theme.TextPrimary

@Composable
fun ChaGokButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false, // 로딩 상태 추가 가능
    shape: Shape = RoundedCornerShape(20.dp)
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryColor, // 활성화 시 배경색
            contentColor = MaterialTheme.colorScheme.onPrimary, // 활성화 시 글자색
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant, // 비활성화 시 배경색
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant // 비활성화 시 글자색
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(text = text, style = ChaGokTextStyle.Subtitle1)
        }
    }
}

// 추가로 필요한 import들
@Composable
fun ChaGokOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    shape: Shape = RoundedCornerShape(20.dp),
    borderColor: Color = TextDisabled // 기본 테두리 색상
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && !isLoading,
        shape = shape,
        // 테두리 설정: 두께와 색상
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) borderColor else MaterialTheme.colorScheme.surfaceVariant
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = borderColor,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = borderColor
            )
        } else {
            Text(
                text = text,
                style = ChaGokTextStyle.Subtitle1,
                color = TextPrimary
            )
        }
    }
}

@Composable
@Preview
fun ChaGokButtonPreview() {
    ChaGokTheme {
        ChaGokButton(
            text = "시작하기",
            onClick = { },
            modifier = Modifier
                .width(358.dp)
                .height(54.dp)

        )
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
fun ChaGokOutlinedButtonPreview() {
    ChaGokTheme {
        ChaGokOutlinedButton(
            text = "시작하기",
            onClick = { },
            modifier = Modifier
                .width(358.dp)
                .height(54.dp)

        )
    }
}