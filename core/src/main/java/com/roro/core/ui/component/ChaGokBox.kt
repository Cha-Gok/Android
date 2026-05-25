package com.roro.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roro.core.domain.model.FileType
import com.roro.core.ui.theme.BoxBackground
import com.roro.core.ui.theme.ChaGokTextStyle
import com.roro.core.ui.theme.ChaGokTheme
import com.roro.core.ui.theme.Gray200
import com.roro.core.ui.theme.Gray500
import com.roro.core.ui.theme.Gray700
import com.roro.core.ui.theme.Gray850
import com.roro.core.ui.theme.PrimaryColor
import com.roro.core.ui.theme.Purple900
import com.roro.core.ui.theme.TextDisabled
import com.roro.core.ui.theme.TextPrimary
import com.roro.core.ui.theme.TextSecondary
import com.roro.core.ui.theme.TextTertiary

@Composable
fun ChaGokBox(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int = 0,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    icon: ImageVector = Icons.Default.Error,
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
            .size(92.dp, 120.dp)
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
                    imageVector = icon,
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
            if (text != "최근 기록") {
                Text(
                    text = count.toString(),
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ChaGokBoxSmall(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    icon: ImageVector = Icons.Default.Error,
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
            .size(116.dp, 38.dp)
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .background(backgroundColor)
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            // 상단 아이콘 + 텍스트
            Row(
                verticalAlignment = Alignment.CenterVertically, // 아이콘과 텍스트의 높이 중앙을 맞춤
                horizontalArrangement = Arrangement.Center      // 가로 내용물들을 중앙에 모음
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) TextPrimary else TextDisabled,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = text,
                    color = if (isSelected) TextPrimary else TextDisabled,
                    style = ChaGokTextStyle.Body2,
                )
            }
        }
    }
}

@Composable
fun ChaGokFolderBox(
    text: String,
    count: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    icon: ImageVector = Icons.Outlined.Folder,
    shape: Shape = RoundedCornerShape(20.dp)
) {
    val backgroundColor = BoxBackground

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(53.dp)
            .clip(shape)
            .background(color = backgroundColor, shape = shape)
            .then(
                if (isSelected) Modifier.border(1.dp, Purple900, shape)
                else Modifier
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // 리플 효과 제거
                onClick = onClick
            )
            .padding(horizontal = 16.dp), // 양 끝 여백
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // 양 끝으로 배치
        ) {
            // 왼쪽 영역: 아이콘 + 폴더 이름
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    color = TextSecondary,
                    style = ChaGokTextStyle.Body2,
                )
            }

            // 오른쪽 영역: 개수
            Text(
                text = count,
                color = TextSecondary,
                style = ChaGokTextStyle.Body2,
            )
        }
    }
}

@Composable
fun ChaGokFileListBox(
    title: String,
    time: String,
    duration: String,
    summaryStatus: SummaryStatus,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectedChange: (Boolean) -> Unit = {},
    shape: Shape = RoundedCornerShape(20.dp)
) {
    // 1. 보더 색상을 더 밝게, 두께를 더 두껍게 설정
    val borderColor = if (isSelected) PrimaryColor else Gray700
    val borderStroke = if (isSelected) 2.dp else 1.dp

    val tagColor = when (summaryStatus) {
        SummaryStatus.COMPLETED -> Color(0xFF7B4FCC)
        SummaryStatus.NONE -> Color(0xFF3D3D4E)
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(118.dp)
            .clip(shape) // 1. 먼저 자르기
            .background(BoxBackground) // 2. 배경 채우기
            .border(borderStroke, borderColor, shape) // 3. 그 위에 보더 그리기
            .clickable {
                if (isSelectionMode) onSelectedChange(!isSelected)
                else onClick()
            }
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. 선택 모드
            if (isSelectionMode) {
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(16.dp) // 체크박스 크기
                            .clip(CircleShape) // 동그라미 모양
                            .background(if (isSelected) PrimaryColor else Gray850),
                        contentAlignment = Alignment.Center

                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp, 12.dp)
                            )
                        }
                    }
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = title, style = ChaGokTextStyle.Title2, color = TextPrimary)
                Text(text = "$time · $duration", style = ChaGokTextStyle.Body1, color = TextSecondary)
                Box(
                    modifier = Modifier
                        .size(66.dp, 27.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(Gray200)
                        .border(1.dp, Gray500, RoundedCornerShape(99.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = summaryStatus.label, style = ChaGokTextStyle.Label, color = TextTertiary)
                }
            }
        }
    }
}

@Composable
fun ChaGokTrashBox(
    title: String,
    firstText: String,
    secondText: String,
    type: FileType,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onSelectedChange: (Boolean) -> Unit = {},
    shape: Shape = RoundedCornerShape(20.dp)
) {
    // 1. 보더 색상을 더 밝게, 두께를 더 두껍게 설정
    val borderColor = if (isSelected) PrimaryColor else Gray700
    val borderStroke = if (isSelected) 2.dp else 1.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(85.dp)
            .clip(shape) // 1. 먼저 자르기
            .background(BoxBackground) // 2. 배경 채우기
            .border(borderStroke, borderColor, shape) // 3. 그 위에 보더 그리기
            .clickable {
                if (isSelectionMode) onSelectedChange(!isSelected)
                else onClick()
            }
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. 선택 모드
            if (isSelectionMode) {
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(16.dp) // 체크박스 크기
                            .clip(CircleShape) // 동그라미 모양
                            .background(if (isSelected) PrimaryColor else Gray850),
                        contentAlignment = Alignment.Center

                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp, 12.dp)
                            )
                        }
                    }
                }
            }
            Icon(
                imageVector = if (type == FileType.FOLDER) Icons.Outlined.Folder else Icons.Outlined.Mic,
                tint = TextSecondary,
                contentDescription = null,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = title, style = ChaGokTextStyle.Title2, color = TextPrimary)
                Text(text = "$firstText · $secondText", style = ChaGokTextStyle.Body1, color = TextSecondary)
            }
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
                icon = Icons.Default.Schedule,
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
                icon = Icons.Outlined.Folder,
                modifier = Modifier
                    .width(92.dp)
                    .height(120.dp)
            )

        }
    }
}

@Preview(showBackground = true, backgroundColor = 0x121212)
@Composable
fun ChaGokBoxSmallPreview() {
    ChaGokTheme {
        Row {
            ChaGokBoxSmall(
                text = "최근 기록",
                isSelected = true,
                onClick = {},
                icon = Icons.Default.Schedule,
                modifier = Modifier
                    .width(116.dp)
                    .height(38.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            ChaGokBoxSmall(
                text = "기본 폴더",
                isSelected = false,
                onClick = {},
                icon = Icons.Outlined.Folder,
                modifier = Modifier
                    .width(116.dp)
                    .height(38.dp)
            )
        }
    }
}

@Composable
fun ChaGokItemBox(
    modifier: Modifier = Modifier,
    title: String,
    createAt: String,
    type: FileType,
    count: String? = null,
    duration: String? = null,
    folderName: String? = null,
    onClick: () -> Unit = {},
    shape: Shape = RoundedCornerShape(20.dp)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(85.dp) // 세 줄일 경우 높이가 타이트할 수 있으니 디자인에 따라 조절 필요
            .clip(shape)
            .background(BoxBackground)
            .border(1.dp, Gray700, shape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp), // 상하 패딩 살짝 조절
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. 아이콘
            Icon(
                imageVector = if (type == FileType.FOLDER) Icons.Outlined.Folder else Icons.Outlined.Mic,
                tint = TextSecondary,
                contentDescription = null,
                modifier = Modifier.padding(end = 16.dp)
            )

            // 2. 텍스트 영역
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center // 중앙 정렬
            ) {
                // [1행] 제목
                Text(
                    text = title,
                    style = ChaGokTextStyle.Title2,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // [2행 & 3행] 타입에 따른 분기
                when (type) {
                    FileType.FOLDER -> {
                        // 폴더는 생성일만 표시
                        Text(
                            text = createAt,
                            style = ChaGokTextStyle.Body1,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }

                    FileType.VOICE_NOTE -> {
                        // 음성메모 2행: 날짜 · 길이
                        Text(
                            text = "$createAt${if (duration != null) " · $duration" else ""}",
                            style = ChaGokTextStyle.Body1,
                            color = TextSecondary,
                            maxLines = 1
                        )

                        // 음성메모 3행: 폴더명 (있는 경우에만)
                        if (!folderName.isNullOrBlank()) {
                            Text(
                                text = folderName,
                                style = ChaGokTextStyle.Label, // 폴더명은 조금 더 작게 표현하는 것이 가독성에 좋음
                                color = TextTertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 3. 오른쪽 영역 (폴더일 때만 개수 표시)
            if (type == FileType.FOLDER && count != null) {
                Text(
                    text = count,
                    style = ChaGokTextStyle.Body2,
                    color = TextTertiary,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0x121212)
@Composable
fun ChaGokFolderBoxPreview() {
    ChaGokTheme {
        Row {
            ChaGokFolderBox(
                text = "회의록",
                onClick = {},
                count = "0",
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0x121212)
@Composable
fun ChaGokFileBoxPreview() {
    ChaGokTheme {
        Column {
            ChaGokFileListBox(
                title = "회의록",
                time = "오후 3:23",
                summaryStatus = SummaryStatus.COMPLETED,
                duration = "2시간 12분",
            )
            ChaGokFileListBox(
                title = "회의록",
                time = "오후 3:23",
                isSelectionMode = true,
                summaryStatus = SummaryStatus.COMPLETED,
                duration = "2시간 12분",
            )
            ChaGokFileListBox(
                title = "회의록",
                time = "오후 3:23",
                isSelectionMode = true,
                isSelected = true,
                summaryStatus = SummaryStatus.COMPLETED,
                duration = "2시간 12분",
            )
            ChaGokTrashBox(
                title = "회의록",
                firstText = "오후 3:23",
                isSelectionMode = true,
                isSelected = true,
                type = FileType.FOLDER,
                secondText = "1개월 전 삭제",
            )
            ChaGokItemBox(
                title = "회의록",
                createAt = "2025.02.03",
                type = FileType.FOLDER,
                count = "3",
                onClick = { },
            )
        }
    }
}