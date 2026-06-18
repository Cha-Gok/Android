package com.roro.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roro.core.domain.model.SortType
import com.roro.core.ui.theme.ChaGokTextStyle
import com.roro.core.ui.theme.ChaGokTheme
import com.roro.core.ui.theme.Gray200
import com.roro.core.ui.theme.Gray850
import com.roro.core.ui.theme.TextDisabled
import com.roro.core.ui.theme.TextPrimary
import com.roro.core.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 1. 새롭게 정의된 범용 메뉴 아이템
 */
data class ChaGokMenuItem(
    val text: String, val textColor: Color = Color.White, // 기본 하얀색
    val isSelected: Boolean? = null,    // null: 체크 공간 미사용
    val hasDividerBefore: Boolean = false, // true: 아이템 상단에 구분선 표시
    val onClick: () -> Unit
)

/**
 * 2. [신규] 범용 탑바 (팀원들이 차례로 갈아탈 대상)
 * 아이콘 유무에 따라 유연하게 대응합니다.
 */
@Composable
fun ChaGokTopBarV2(
    title: String,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    backButtonIcon: ImageVector = Icons.Default.ArrowBackIosNew,
    onBackClick: () -> Unit = {},
    firstActionIcon: ImageVector? = null,
    firstActionDescription: String? = null,
    onFirstActionClick: () -> Unit = {},
    secondActionIcon: ImageVector? = null,
    secondActionDescription: String? = null,
    onSecondActionClick: () -> Unit = {},
    secondActionTrailingContent: @Composable () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBackButton) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = backButtonIcon, contentDescription = "뒤로가기", tint = Color.White
                )
            }
        }

        Text(
            text = title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)
        )

        firstActionIcon?.let {
            TopBarIcon(
                imageVector = it, contentDescription = firstActionDescription, onClick = onFirstActionClick
            )
        }

        Box {
            secondActionIcon?.let {
                TopBarIcon(
                    imageVector = it, contentDescription = secondActionDescription, onClick = onSecondActionClick
                )
            }
            secondActionTrailingContent()
        }
    }
}

/**
 * 3. [신규] 범용 드롭다운 메뉴
 */
@Composable
fun ChaGokMoreMenu(
    expanded: Boolean, onDismissRequest: () -> Unit, items: List<ChaGokMenuItem>, modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    DropdownMenu(
        expanded = expanded, onDismissRequest = onDismissRequest, modifier = modifier.width(180.dp), shape = RoundedCornerShape(20.dp), containerColor = Gray200
    ) {
        // 리스트 중 하나라도 선택 기능(isSelected != null)을 사용하는지  확인
        val showCheckColumn = items.any { it.isSelected != null }

        items.forEach { item ->
            // 구분서 표시 여부
            if (item.hasDividerBefore) {
                HorizontalDivider(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .padding(horizontal = 16.dp), color = TextDisabled
                )
            }
            DropdownMenuItem(text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showCheckColumn) {
                        if (item.isSelected == true) {
                            Icon(
                                imageVector = Icons.Default.Check, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                    Text(
                        text = item.text, color = item.textColor, style = ChaGokTextStyle.Body2
                    )
                }
            }, contentPadding = PaddingValues(16.dp, vertical = 8.dp), onClick = {
                onDismissRequest()
                scope.launch {
                    delay(20L)
                    item.onClick()
                }
            })
        }
    }
}

@Deprecated("새로운 ChaGokTopBarV2를 사용하세요. 이 컴포넌트는 제거될 예정입니다.")
@Composable
fun ChaGokTopBar(
    title: String,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    firstActionIcon: ImageVector = Icons.Default.Search,
    firstActionDescription: String = "검색",
    onFirstActionClick: () -> Unit = {},
    secondActionIcon: ImageVector = Icons.Outlined.Settings,
    secondActionDescription: String = "설정",
    onSecondActionClick: () -> Unit = {},
    secondActionTrailingContent: @Composable () -> Unit = {} // ← 추가
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBackButton) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew, contentDescription = "뒤로가기", tint = Color.White
                )
            }
        }

        Text(
            text = title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onFirstActionClick) {
            Icon(
                imageVector = firstActionIcon, contentDescription = firstActionDescription, tint = Color.White
            )
        }

        // 설정 아이콘 + 드롭다운 앵커를 Box로 묶기
        Box {
            IconButton(onClick = onSecondActionClick) {
                Icon(
                    imageVector = secondActionIcon, contentDescription = secondActionDescription, tint = Color.White
                )
            }
            secondActionTrailingContent() // ← 드롭다운이 아이콘 바로 아래 붙도록 함
        }
    }
}

/**
 * 차곡 프로젝트 공통 탑바
 *
 * @param title 화면 제목
 * @param showBackButton 뒤로가기 버튼 표시 여부
 * @param onBackClick 뒤로가기 클릭 이벤트
 * @param actions 우측에 배치될 아이콘들 (RowScope를 사용하여 여러 개 배치 가능)
 */
@Deprecated("새로운 ChaGokTopBarV2를 사용하세요. 이 컴포넌트는 제거될 예정입니다.")
@Composable
fun ChaGokTopBar2(
    title: String,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    backIcon: ImageVector = Icons.Default.ArrowBackIosNew,
    onBackClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {} // 우측 아이콘들을 자유롭게 넣을 수 있는 슬롯
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp) // 표준 탑바 높이
            .padding(horizontal = 12.dp), // 아이콘 버튼의 내부 패딩을 고려하여 조절
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 왼쪽 영역: 뒤로가기 버튼
        if (showBackButton) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = backIcon, contentDescription = "뒤로가기", tint = Color.White, modifier = Modifier.size(20.dp)
                )
            }
        } else {
            // 뒤로가기가 없을 때 왼쪽 여백 (디자인에 따라 조절)
            Spacer(modifier = Modifier.size(8.dp))
        }

        // 2. 중간 영역: 타이틀
        Text(
            text = title, color = TextPrimary, style = ChaGokTextStyle.Header2, modifier = Modifier
                .weight(1f)
                .padding(start = if (showBackButton) 0.dp else 4.dp)
        )

        // 3. 오른쪽 영역: 액션 버튼들
        Row(
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End, content = actions
        )
    }
}

@Deprecated("새로운 ChaGokMoreMenu 사용하세요. 이 컴포넌트는 제거될 예정입니다.")
@Composable
fun TopBarMoreMenu(
    currentSortType: SortType, onSortByCreate: () -> Unit, onSortByUpdate: () -> Unit, onSelectAll: () -> Unit, onSelectMode: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TopBarIcon(
            imageVector = Icons.Outlined.MoreVert, onClick = { expanded = true })

        DropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color(0xFF252525)) // 어두운 테마 배경
        ) {
            DropdownMenuItem(text = { Text("생성일 순", color = Color.White) }, leadingIcon = {
                if (currentSortType == SortType.CREATED_AT) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Gray850)
                } else {
                    // 체크가 없을 때도 공간을 확보하여 텍스트 정렬을 맞춤
                    Spacer(modifier = Modifier.size(24.dp))
                }
            }, onClick = {
                onSortByCreate()
                expanded = false
            })
            DropdownMenuItem(text = { Text("수정일 순", color = Color.White) }, leadingIcon = {
                if (currentSortType == SortType.UPDATED_AT) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Gray850)
                } else {
                    Spacer(modifier = Modifier.size(24.dp))
                }
            }, onClick = { onSortByUpdate(); expanded = false })

            // 구분선
            HorizontalDivider(
                modifier = Modifier.padding(4.dp), color = Color.Gray.copy(alpha = 0.5f)
            )

            DropdownMenuItem(
                text = { Text("전체 선택하기", color = Color.White) }, modifier = Modifier.padding(start = 40.dp), // leadingIcon 공간만큼 패딩
                onClick = { onSelectAll(); expanded = false })
            DropdownMenuItem(
                text = { Text("선택하기", color = Color.White) }, modifier = Modifier.padding(start = 40.dp), // leadingIcon 공간만큼 패딩
                onClick = { onSelectMode(); expanded = false })
        }
    }
}

@Deprecated("새로운 ChaGokMoreMenu 사용하세요. 이 컴포넌트는 제거될 예정입니다.")
@Composable
fun ChaGokSettingsDropdown(
    expanded: Boolean, onDismissRequest: () -> Unit, onLanguageSettingClick: () -> Unit, onTosClick: () -> Unit, modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded, onDismissRequest = onDismissRequest, modifier = modifier.background(Color(0xFF252525)) // 어두운 테마 배경
    ) {
        DropdownMenuItem(text = { Text("녹음 언어 설정", color = Gray850) }, onClick = {
            onLanguageSettingClick()
            onDismissRequest()
        })
        DropdownMenuItem(text = { Text("약관 보기", color = Gray850) }, onClick = {
            onTosClick()
            onDismissRequest()
        })
    }
}

/**
 * 공통으로 자주 쓰이는 탑바용 아이콘 버튼 컴포넌트
 */
@Composable
fun TopBarIcon(
    imageVector: ImageVector, contentDescription: String? = null, onClick: () -> Unit, tint: Color = Color.White
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = imageVector, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(24.dp)
        )
    }
}

// --- Preview 영역 ---

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun MainScreenTopBarPreview() {
    ChaGokTheme {
        // 메인 화면 스타일 (검색 + 설정)
        ChaGokTopBar2(
            title = "차곡", actions = {
                TopBarIcon(imageVector = Icons.Default.Search, onClick = {})
                TopBarIcon(imageVector = Icons.Default.MoreVert, onClick = {})
            })
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun SubScreenTopBarPreview() {
    ChaGokTheme {
        // 서브 화면 스타일 (뒤로가기 + 타이틀만)
        ChaGokTopBar2(
            title = "개인 폴더", showBackButton = true, onBackClick = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun FullActionTopBarPreview() {
    ChaGokTheme {
        // 모든 기능이 포함된 스타일
        ChaGokTopBar2(
            title = "상세 보기", showBackButton = true, actions = {
                TopBarIcon(imageVector = Icons.Default.Search, onClick = {})
                // 필요한 경우 여기에 직접 Box와 DropdownMenu 등을 넣을 수 있음
            })
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
            title = "차곡", showBackButton = true, secondActionIcon = Icons.Default.MoreVert, secondActionDescription = "메뉴"
        )
    }
}