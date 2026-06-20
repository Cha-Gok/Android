package com.roro.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.roro.core.domain.model.SummaryStatus
import com.roro.core.ui.theme.ChaGokTheme

@Composable
fun ChaGokNoteList(
    title: String,
    time: String,
    summaryStatus: SummaryStatus,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val tagColor = when (summaryStatus) {
        SummaryStatus.NONE -> Color(0xFF3D3D4E)
        SummaryStatus.INSUFFICIENT -> Color(0xFF3D3D4E)
        SummaryStatus.GENERATING -> Color(0xFF5A4A7A)
        SummaryStatus.SUCCESS -> Color(0xFF7B4FCC)
        SummaryStatus.FAIL -> Color(0xFF3D3D4E)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E1E2E))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = time,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(tagColor)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(text = summaryStatus.label, color = Color.White, fontSize = 12.sp)
        }
    }
}

@Preview
@Composable
private fun ChaGokNoteCardCompletedPreview() {
    ChaGokTheme {
        ChaGokNoteList(
            title = "오전 취업 관련 강의",
            time = "오후 3:23 · 2시간 12분",
            summaryStatus = SummaryStatus.SUCCESS,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview
@Composable
private fun ChaGokNoteCardNonePreview() {
    ChaGokTheme {
        ChaGokNoteList(
            title = "오전 취업 관련 강의",
            time = "오후 3:23 · 2시간 12분",
            summaryStatus = SummaryStatus.NONE,
            modifier = Modifier.padding(16.dp)
        )
    }
}
