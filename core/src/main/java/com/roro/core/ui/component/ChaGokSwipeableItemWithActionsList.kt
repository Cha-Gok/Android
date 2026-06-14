package com.roro.core.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ChaGokSwipeableFolderItem(
    id: Any,
    text: String,
    count: String,
    isRevealed: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ChaGokSwipeableItemWithActionsList(
        isRevealed = isRevealed,
        // 끝까지 밀었을 때 실행할 동작 (삭제)
        onFullSwipe = onDelete,
        actions = {
            // 수정 Chip
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable {
                        onEdit()
                        onCollapse()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "수정",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp)) // 간격 조금 더 넓힘

            // 삭제 Chip
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE24444).copy(alpha = 0.8f))
                    .clickable {
                        onDelete()
                        onCollapse()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        onExpanded = onExpand,
        onCollapsed = onCollapse,
        modifier = modifier
    ) {
        ChaGokFolderBox(
            text = text,
            count = count,
            onClick = { if (isRevealed) onCollapse() else onClick() },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF13003F), shape = RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp)),
        )
    }
}

@Composable
fun ChaGokSwipeableFileItem(
    title: String,
    time: String,
    duration: String,
    summary: SummaryStatus,
    isRevealed: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ChaGokSwipeableItemWithActionsList(
        isRevealed = isRevealed,
        // 끝까지 밀었을 때 실행할 동작 (삭제)
        onFullSwipe = onDelete,
        actions = {
            // 수정 Chip
//            Box(
//                modifier = Modifier
//                    .size(45.dp)
//                    .clip(CircleShape)
//                    .background(Color.White.copy(alpha = 0.15f))
//                    .clickable {
//                        onEdit()
//                        onCollapse()
//                    },
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Edit,
//                    contentDescription = "수정",
//                    tint = Color.White,
//                    modifier = Modifier.size(20.dp)
//                )
//            }

            Spacer(modifier = Modifier.width(12.dp)) // 간격 조금 더 넓힘

            // 삭제 Chip
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(44.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFE24444).copy(alpha = 0.8f))
                    .clickable {
                        onDelete()
                        onCollapse()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        onExpanded = onExpand,
        onCollapsed = onCollapse,
        modifier = modifier
    ) {
        ChaGokFileListBox(
            title = title,
            time = time,
            duration = duration,
            summaryStatus = summary,
            onClick =  onClick ,
            isSelectionMode = isSelectionMode,
            isSelected = isSelected,
            onSelectedChange = onChange ,
            modifier = Modifier
                .fillMaxWidth()
                // [수정 포인트 1] 선택 상태에 따른 테두리 추가
                .border(
                    width = 2.dp,
                    color = if (isSelectionMode && isSelected) Color(0xFF6B4EFF) else Color.Transparent,
                    shape = RoundedCornerShape(20.dp)
                )
                // [수정 포인트 2] 배경색과 클립 적용
                .background(Color(0xFF13003F), shape = RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp)),
        )
    }
}

@Composable
fun ChaGokSwipeableItemWithActionsList(
    isRevealed: Boolean,
    actions: @Composable RowScope.() -> Unit,
    onExpanded: () -> Unit,
    onCollapsed: () -> Unit,
    onFullSwipe: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var contextMenuWidth by remember { mutableFloatStateOf(0f) }
    var screenWidth by remember { mutableFloatStateOf(0f) }
    val offset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // 밀도 계산을 위한 density 추출
    val density = LocalDensity.current

    LaunchedEffect(isRevealed, contextMenuWidth) {
        if (isRevealed) {
            offset.animateTo(-contextMenuWidth)
        } else {
            offset.animateTo(0f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .onSizeChanged { screenWidth = it.width.toFloat() }
    ) {
        // 1. 하단 액션 버튼 영역
        Row(
            modifier = Modifier
                .padding(end = 16.dp) // 오른쪽 끝 여백
                .onSizeChanged {
                    // 중요: 하드코딩된 32f 대신 DP를 PX로 변환하여 더해줌
                    // 수정 버튼 왼쪽에도 여유 공간이 생기도록 16dp(오른쪽) + 16dp(왼쪽) = 32dp 확보
                    with(density) {
                        contextMenuWidth = it.width.toFloat() + 24.dp.toPx()
                    }
                }
                .fillMaxHeight()
                .align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )

        // 2. 상단 메인 컨텐츠
        Box(
            modifier = Modifier
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .fillMaxWidth()
                .pointerInput(contextMenuWidth, screenWidth) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                // 조금 더 시원하게 밀리도록 40f 정도의 오버스크롤 허용
                                val newOffset = (offset.value + dragAmount)
                                    .coerceIn(-screenWidth, 0f)
                                offset.snapTo(newOffset)
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                val threshold = screenWidth * 0.6f

                                when {
                                    onFullSwipe != null && abs(offset.value) > threshold -> {
                                        offset.animateTo(-screenWidth)
                                        onFullSwipe()
                                    }
                                    // 걸리는 구간 (Snap Point)
                                    abs(offset.value) > contextMenuWidth / 2f -> {
                                        onExpanded()
                                        offset.animateTo(-contextMenuWidth)
                                    }

                                    else -> {
                                        onCollapsed()
                                        offset.animateTo(0f)
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}


@Composable
@Preview(showBackground = true,) // 배경색 설정
fun ChaGokSwipePreview() {
    // 미리보기에서 스와이프 상태를 확인하기 위한 가상 데이터 리스트
    val folderList = remember {
        mutableStateListOf(
            Pair(1L, "여행 사진"),
            Pair(2L, "업무 문서"),
            Pair(3L, "공부 자료")
        )
    }

    // 현재 어떤 아이템이 열려 있는지 관리하는 상태
    var revealedFolderId by remember { mutableStateOf<Long?>(null) }
    // [중요] MutableState의 Set이 변경될 때 UI가 리컴포지션 되도록 보장
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        folderList.forEach { (id, name) ->
            ChaGokSwipeableFolderItem(
                id = id,
                text = name,
                count = (id * 3).toString(), // 임시 개수
                isRevealed = revealedFolderId == id,
                onExpand = { revealedFolderId = id },
                onCollapse = { if (revealedFolderId == id) revealedFolderId = null },
                onEdit = {
                    /* 미리보기이므로 로그만 출력 */
                    println("$name 수정 클릭")
                },
                onDelete = {
                    /* 리스트에서 제거하는 동작 확인 */
                    folderList.removeIf { it.first == id }
                },
                onClick = {
                    println("$name 폴더 진입")
                }
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            folderList.forEach { (id, name) ->
                ChaGokSwipeableFileItem(
                    title = name,
                    time = "오후 11:11",
                    duration = "03:00",
                    summary = SummaryStatus.COMPLETED,
                    isRevealed = revealedFolderId == id,
                    onExpand = { revealedFolderId = id },
                    onCollapse = { if (revealedFolderId == id) revealedFolderId = null },
                    isSelectionMode = true,
                    isSelected = selectedIds.contains(id), // 여기서 상태 체크
                    onDelete = { folderList.removeIf { it.first == id } },
                    onClick = { /* 클릭 로직 */ },
                    onChange = { isChecked ->
                        // [수정 포인트 3] Set 상태 업데이트
                        selectedIds = if (isChecked) {
                            selectedIds + id
                        } else {
                            selectedIds - id
                        }
                    }
                )
            }
        }
    }
}