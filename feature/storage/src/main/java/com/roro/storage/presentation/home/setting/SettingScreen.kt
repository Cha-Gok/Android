package com.roro.storage.presentation.home.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.roro.core.datastore.Language
import com.roro.core.ui.component.ChaGokBackground
import com.roro.core.ui.component.ChaGokLanguageDialog
import com.roro.core.ui.component.ChaGokTopBar
import com.roro.core.ui.component.ChaGokTopBarV2
import com.roro.core.ui.theme.ChaGokTextStyle
import com.roro.core.ui.theme.TextPrimary
import com.roro.core.ui.theme.TextTertiary
import com.roro.core.util.toast
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.roro.core.navigation.Routes
import com.roro.core.ui.theme.PrimaryColor

@Composable
fun SettingScreen(
    navController: NavController,
    viewModel: SettingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onIntent(SettingIntent.LoadData)
        viewModel.effect.collect { effect ->
            when (effect) {
                SettingEffect.NavigateBack -> navController.popBackStack()
                is SettingEffect.ShowToast -> context.toast(effect.message)
            }
        }
    }

    SettingScreenContent(
        uiState = uiState,
        onBackClick = { viewModel.onIntent(SettingIntent.NavigateBack) },
        onLanguageSelect = { viewModel.onIntent(SettingIntent.SelectLanguage(it)) },
        onConfirmLanguage = { viewModel.onIntent(SettingIntent.ConfirmLanguage) },
        onDeleteGemma = { viewModel.onIntent(SettingIntent.DeleteGemmaModel) },
        onTosClick = { navController.navigate(Routes.webView("https://sunset-bar-890.notion.site/369d9da368aa8033be62f317299c07f2?pvs=74")) },
        onPrivacyClick = { navController.navigate(Routes.webView("https://sunset-bar-890.notion.site/369d9da368aa80538cced7f6c56e339a?pvs=74")) },
        onContactClick = { navController.navigate(Routes.webView("https://docs.google.com/forms/d/e/1FAIpQLSeevBvqUuIG4yBEos3T6KEZc_R1GgbMLAZYG9iHTc4JMv7DIg/viewform")) },
    )
}

@Composable
private fun SettingScreenContent(
    uiState: SettingUiState,
    onBackClick: () -> Unit,
    onLanguageSelect: (Language) -> Unit,
    onConfirmLanguage: () -> Unit,
    onDeleteGemma: () -> Unit,
    onTosClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onContactClick: () -> Unit,
) {
    ChaGokBackground {
        Column(modifier = Modifier.fillMaxSize()) {

            ChaGokTopBarV2(
                title = "설정",
                showBackButton = true,
                onBackClick = onBackClick,
            )


            // 언어 설정
            SettingSection(title = "언어 설정", description = "녹음 기록 언어를 바꿉니다.") {
                Language.entries.forEach { lang ->
                    val label = when (lang) {
                        Language.KOREAN -> "한국어"
                        Language.ENGLISH -> "영어"
                    }
                    SettingRadioItem(
                        label = label,
                        selected = uiState.selectedLanguage == lang,
                        onClick = {
                            onLanguageSelect(lang)
                            onConfirmLanguage()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 음성 인식 모델 설정
            SettingSection(title = "음성 인식 모델 설정") {
                if (uiState.isModelDownloaded) {
                    SettingModelItem(
                        label = "Gemma-4",
                        description = "기기에서 텍스트 변환, 요약, 키워드 추출을 위한 필수 모델이에요.",
                        onDeleteClick = onDeleteGemma
                    )
                } else {
                    Text(
                        text = "다운로드된 모델이 없습니다.",
                        style = ChaGokTextStyle.Body2,
                        color = TextTertiary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 기타 메뉴
            SettingTextItem(label = "이용약관", onClick = onTosClick)
            SettingTextItem(label = "개인정보처리방침", onClick = onPrivacyClick)
            SettingTextItem(label = "고객 문의", onClick = onContactClick)
        }
    }
}

@Composable
private fun SettingSection(
    title: String,
    description: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text(text = title, style = ChaGokTextStyle.Title3, color = TextPrimary)
        description?.let {
            Text(text = it, style = ChaGokTextStyle.Body2, color = TextTertiary)
        }
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun SettingRadioItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E2E))
            .border(
                width = if (selected) 1.dp else 0.5.dp,
                color = if (selected) PrimaryColor else Color(0xFF3D3D4E),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 커스텀 라디오 원
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(PrimaryColor, CircleShape)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = ChaGokTextStyle.Body1,
            color = if (selected) TextPrimary else TextTertiary
        )
    }
}

@Composable
private fun SettingModelItem(
    label: String,
    description: String,
    onDeleteClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E2E))
            .border(0.5.dp, Color(0xFF3D3D4E), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = ChaGokTextStyle.Body1, color = TextPrimary)
            Text(text = description, style = ChaGokTextStyle.Body2, color = TextTertiary)
        }
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = "모델 삭제",
            tint = Color(0xFFE53935),
            modifier = Modifier
                .size(22.dp)
                .clickable(onClick = onDeleteClick)
        )
    }
}

@Composable
private fun SettingTextItem(
    label: String,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        style = ChaGokTextStyle.Body1,
        color = TextPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    )
}