package com.roro.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roro.core.ui.theme.BoxBackground
import com.roro.core.ui.theme.ChaGokTheme
import com.roro.core.ui.theme.Gray300
import com.roro.core.ui.theme.PrimaryColor
import com.roro.core.ui.theme.Purple900
import com.roro.core.ui.theme.TextDisabled
import com.roro.core.ui.theme.TextPrimary

@Composable
fun ChaGokBox(
    text: String,
    count: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(20.dp)
) {
    val borderColor = if (isSelected) {
        Color(0xFFB388FF) // 보라 강조
    } else {
        Color.Gray.copy(alpha = 0.3f)
    }

    val backgroundColor = BoxBackground

    Box(
        modifier = modifier
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .background(backgroundColor)
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // 상단 아이콘 + 텍스트
            Column {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = if (isSelected) TextPrimary else TextDisabled
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = text,
                    color = if (isSelected) TextPrimary else TextDisabled,
                    fontSize = 14.sp
                )
            }

            // 하단 count
            Text(
                text = count.toString(),
                color = Color.LightGray,
                fontSize = 12.sp
            )
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0x121212)
@Composable
fun ChaGokBoxPreview() {
    ChaGokTheme {
        Row {
            ChaGokBox(
                text = "최근 기록",
                count = 2,
                isSelected = true,
                onClick = {},
                modifier = Modifier
                    .width(92.dp)
                    .height(120.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            ChaGokBox(
                text = "기본 폴더",
                count = 0,
                isSelected = false,
                onClick = {},
                modifier = Modifier
                    .width(92.dp)
                    .height(120.dp)
            )
        }
    }
}