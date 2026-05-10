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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.roro.core.datastore.Language
import androidx.compose.ui.window.DialogProperties
import com.roro.core.ui.theme.ChaGokTextStyle
import com.roro.core.ui.theme.Danger
import com.roro.core.ui.theme.Gray100
import com.roro.core.ui.theme.PrimaryColor
import com.roro.core.ui.theme.TextPrimary
import com.roro.core.ui.theme.TextTertiary

@Composable
fun ChagokDialog(
    title: String,
    description: String,
    dismissText: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmColor: Color = Color(0xFF7B4FCC), // 기본값 기존 보라색, 필요시 빨간색 등으로 교체
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    color = TextPrimary,
                    style = ChaGokTextStyle.Title2,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = description,
                    color = TextPrimary,
                    style = ChaGokTextStyle.Body2,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                        Text(text = dismissText, color = Color.White, fontSize = 14.sp)
                    }
                    Box(
                        modifier = Modifier
                            .width(107.dp)
                            .height(46.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .border(width = 0.5.dp, color = Color(0xFF7B4FCC), shape = RoundedCornerShape(20.dp))
                            .background(confirmColor)
                            .clickable(onClick = onConfirm)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = confirmText, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun ChaGokDialogCreateFolder(
    title: String,
    description: String,
    dismissText: String,
    confirmText: String,
    inputText: String = "",
    onValueChange: (String) -> Unit = {},
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                if (title == "새 폴더" || title == "폴더 이름 수정") { // 수정 예정
                    TextField(
                        value = inputText,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        placeholder = {
                            Text(text = "폴더 이름을 적어주세요", style = ChaGokTextStyle.Body1, color = TextTertiary)
                        },
                        colors = TextFieldDefaults.colors(
                            // 글자 색상을 흰색으로 명시
                            focusedTextColor = TextTertiary,
                            unfocusedTextColor = TextTertiary,
                            focusedContainerColor = Gray100,
                            unfocusedContainerColor = Gray100,

                            cursorColor = TextTertiary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,

                            ),
                        singleLine = true

                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
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
                        Text(text = dismissText, color = Color.White, fontSize = 14.sp)
                    }
                    Box(
                        modifier = Modifier
                            .width(107.dp)
                            .height(46.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .border(width = 0.5.dp, color = if (confirmText == "삭제") Danger else Color(0xFF7B4FCC), shape = RoundedCornerShape(20.dp))
                            .background(if (confirmText == "삭제") Danger else Color(0xFF7B4FCC))
                            .clickable(onClick = onConfirm)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = confirmText, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun ChaGokLanguageDialog(
    title: String,
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    dismissText: String,
    confirmText: String,
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
                    color = TextPrimary,
                    style = ChaGokTextStyle.Title2,
                    textAlign = TextAlign.Center
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LanguageRadioButton(
                        text = "한국어",
                        isSelected = selectedLanguage == Language.KOREAN,
                        onClick = { onLanguageSelected(Language.KOREAN) },
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    LanguageRadioButton(
                        text = "영어",
                        isSelected = selectedLanguage == Language.ENGLISH,
                        onClick = { onLanguageSelected(Language.ENGLISH) },
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF3D3D4E))
                            .clickable(onClick = { onDismiss() })
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = dismissText, color = TextTertiary, style = ChaGokTextStyle.Body1)
                    }
                    Box(
                        modifier = Modifier
                            .width(107.dp)
                            .height(46.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .border(width = 0.5.dp, color = Color(0xFF7B4FCC), shape = RoundedCornerShape(20.dp))
                            .background(Color(0xFF7B4FCC))
                            .clickable(onClick = { onConfirm() })
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = confirmText, color = TextPrimary, style = ChaGokTextStyle.Body1)
                    }
                }
            }
        }
    }
}

/**
 * 라디오 버튼 커스텀 컴포넌트
 */
@Composable
fun LanguageRadioButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // 커스텀 라디오 버튼 구현
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    // 선택되었을 때만 배경을 흰색으로 꽉 채움
                    color = Color.White,
                    shape = CircleShape
                )
//                .border(
//                    width = 1.5.dp,
//                    // 미선택 시에는 회색 테두리, 선택 시에는 흰색 테두리
//                    color = if (selected) Color.White else Gray400,
//                    shape = CircleShape
//                ),
            ,
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                // 선택되었을 때 흰색 배경 위에 올라가는 보라색 점
                Box(
                    modifier = Modifier
                        .size(12.dp) // 점 크기를 10dp 정도로 키우면 더 잘 보입니다
                        .background(PrimaryColor, CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = if (isSelected) TextPrimary else TextTertiary,
            style = ChaGokTextStyle.Subtitle1
        )
    }
}

@Preview(name = "수정후")
@Composable
fun ChaGokDialogPreview() {
    ChagokDialog(
        title = "마이크 권한이 필요해요",
        description = "설정에서 마이크 권한을 허용해주세요.",
        dismissText = "나중에",
        confirmText = "설정으로 이동",
        onDismiss = {},
        onConfirm = {}
    )
}

@Preview(name = "새 폴더")
@Composable
fun ChaGokDialogCreateFolderPreview() {
    ChaGokDialogCreateFolder(
        title = "새 폴더",
        description = "새로 만들 폴더의 이름을\n입력해주세요.",
        dismissText = "취소",
        confirmText = "만들기",
        onDismiss = {},
        onConfirm = {}
    )
}

@Preview(name = "언어 선택")
@Composable
fun ChaGokDialogLanguagePreview() {
    ChaGokLanguageDialog(
        title = "녹음 언어 변경",
        dismissText = "취소",
        confirmText = "저장하기",
        onDismiss = {},
        onConfirm = {},
        selectedLanguage = Language.KOREAN,
        onLanguageSelected = {},
    )
}