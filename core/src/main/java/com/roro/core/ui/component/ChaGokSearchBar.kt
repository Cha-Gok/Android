package com.roro.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.roro.core.R
import com.roro.core.ui.theme.ChaGokTextStyle
import com.roro.core.ui.theme.Gray850
import com.roro.core.ui.theme.TextDisabled

@Composable
fun ChaGokSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPreview = LocalInspectionMode.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. 검색 입력 영역 (배경 투명 + 테두리)
        Row(
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .border(
                    width = 1.dp,
                    color = Gray850, // 테두리 색상 (원하시는 회색으로 변경 가능)
                    shape = RoundedCornerShape(20.dp)
                )
                .background(
                    color = Color.Transparent, // 배경색 투명
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = modifier.padding(start = 16.dp),
                painter = if (isPreview) {
                    rememberVectorPainter(Icons.Default.Search)
                } else {
                    painterResource(id = R.drawable.ic_search)
                },
                tint = Gray850,
                contentDescription = null,
            )

            Spacer(modifier = Modifier.width(8.dp))

            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = ChaGokTextStyle.Body1.copy(color = Color.White),
                singleLine = true,
                cursorBrush = SolidColor(Color.White),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = "검색",
                            style = ChaGokTextStyle.Body1,
                            color = TextDisabled
                        )
                    }
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 2. 분리된 X 버튼 (배경 투명 + 테두리)
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .border(
                    width = 1.dp,
                    color = Gray850,
                    shape = CircleShape
                )
                .background(
                    color = Color.Transparent,
                    shape = CircleShape
                )
        ) {
            Icon(
                painter = if (isPreview) {
                    rememberVectorPainter(Icons.Default.Close)
                } else {
                    painterResource(id = R.drawable.ic_close)
                },
                tint = Gray850,
                contentDescription = "Close",
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212) // 배경이 어두운 앱인 경우 지정
@Composable
fun ChaGokSearchBarPreview() {
    // 테마가 있다면 ChaGokTheme { ... } 으로 감싸주세요
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
    ) {
        // 1. 초기 상태 (힌트 텍스트가 보이는 상태)
        Text(text = "초기 상태", color = White)
        ChaGokSearchBar(
            query = "",
            onQueryChange = {},
            onBackClick = {},
            onSearch = {}
        )

        // 2. 검색어가 입력된 상태
        Text(text = "검색어 입력 상태", color = White)
        ChaGokSearchBar(
            query = "녹음 파일 검색",
            onQueryChange = {},
            onBackClick = {},
            onSearch = {}
        )

        // 3. 실제 동작 확인용 (인터랙티브 프리뷰)
        Text(text = "동작 테스트용", color = White)
        var testQuery by remember { mutableStateOf("") }
        ChaGokSearchBar(
            query = testQuery,
            onQueryChange = { testQuery = it },
            onBackClick = { testQuery = "" },
            onSearch = { /* 검색 실행 시뮬레이션 */ }
        )
    }
}