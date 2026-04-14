package com.roro.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roro.core.ui.theme.ChaGokTheme


@Composable
fun ChaGokTopBar(
    title: String,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    firstActionIcon: ImageVector = Icons.Default.Search,
    firstActionDescription: String = "검색",
    onFirstActionClick: () -> Unit = {},
    secondActionIcon: ImageVector = Icons.Default.Settings,
    secondActionDescription: String = "설정",
    onSecondActionClick: () -> Unit = {},
    secondActionTrailingContent: @Composable () -> Unit = {} // ← 추가
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBackButton) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "뒤로가기",
                    tint = Color.White
                )
            }
        }

        Text(
            text = title,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onFirstActionClick) {
            Icon(
                imageVector = firstActionIcon,
                contentDescription = firstActionDescription,
                tint = Color.White
            )
        }

        // 설정 아이콘 + 드롭다운 앵커를 Box로 묶기
        Box {
            IconButton(onClick = onSecondActionClick) {
                Icon(
                    imageVector = secondActionIcon,
                    contentDescription = secondActionDescription,
                    tint = Color.White
                )
            }
            secondActionTrailingContent() // ← 드롭다운이 아이콘 바로 아래 붙도록 함
        }
    }
}

@Preview
@Composable
private fun ChaGokTopBarDefaultPreview() {
    ChaGokTheme {
        ChaGokTopBar(title = "차곡")
    }
}

@Preview
@Composable
private fun ChaGokTopBarWithBackPreview() {
    ChaGokTheme {
        ChaGokTopBar(
            title = "차곡",
            showBackButton = true,
            secondActionIcon = Icons.Default.MoreVert,
            secondActionDescription = "메뉴"
        )
    }
}