package com.roro.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

enum class RecordingLanguage(val label: String) {
    KOREAN("한국어"),
    ENGLISH("영어")
}

// 언어 변경 시 사용
// 추후 수정 예정
@Composable
fun ChagokRadioDialog(
    title: String,
    options: List<RecordingLanguage>,
    selectedOption: RecordingLanguage,
    onOptionSelected: (RecordingLanguage) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(width = 0.7.dp, color = Color(0xFF7B7B7B), shape = RoundedCornerShape(20.dp))
                .background(Color(0xFF2D2D3A))
                .padding(horizontal = 20.dp, vertical = 32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                // 라디오 버튼
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    options.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                //.padding(horizontal = 8.dp)
                                .clickable { onOptionSelected(option) }
                        ) {
                            RadioButton(
                                selected = selectedOption == option,
                                onClick = { onOptionSelected(option) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF7B4FCC),
                                    unselectedColor = Color.White.copy(alpha = 0.5f)
                                )
                            )
                            Text(text = option.label, color = Color.White, fontSize = 20.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // ChagokDialog 버튼과 동일
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF3D3D4E))
                            .clickable(onClick = onDismiss)
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "취소", color = Color.White, fontSize = 14.sp)
                    }
                    Box(
                        modifier = Modifier
                            .width(107.dp)
                            .height(46.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .border(width = 0.5.dp, color = Color(0xFF7B4FCC), shape = RoundedCornerShape(20.dp))
                            .background(Color(0xFF7B4FCC))
                            .clickable(onClick = onConfirm)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "저장하기", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ChagokRadioDialogPreview() {
    var selected by remember { mutableStateOf(RecordingLanguage.KOREAN) }
    ChagokRadioDialog(
        title = "녹음 언어 변경",
        options = RecordingLanguage.entries,
        selectedOption = selected,
        onOptionSelected = { selected = it },
        onDismiss = {},
        onConfirm = {}
    )
}