package com.roro.onboarding.presentation

import android.Manifest
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.roro.core.datastore.Language
import com.roro.core.navigation.Routes
import com.roro.core.ui.component.ChaGokBackground
import com.roro.core.ui.component.ChaGokButton
import com.roro.core.ui.component.ChaGokOutlinedButton
import com.roro.core.ui.theme.ChaGokTextStyle
import com.roro.core.ui.theme.Gray400
import com.roro.core.ui.theme.Gray900
import com.roro.core.ui.theme.PrimaryColor
import com.roro.core.ui.theme.TextPrimary
import com.roro.onboarding.R
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

private const val ONBOARDING_PAGE_COUNT = 4

@Composable
fun OnBoardingScreen(
    navController: NavController,
) {
    val viewModel: OnBoardingViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val pagerState = rememberPagerState(
        pageCount = { ONBOARDING_PAGE_COUNT }
    )

    BackHandler(enabled = true) {
        if (pagerState.currentPage > 0) {
            viewModel.onIntent(OnboardingIntent.ClickBack)
        } else {
            (context as? Activity)?.finish()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Timber.d("Onboarding 수신 결과 $isGranted")
        viewModel.onIntent(OnboardingIntent.PermissionResult(isGranted))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is OnboardingEffect.ScrollToPage -> {
                    Timber.d("Onboarding effect ScrollToPage = ${effect.index}")
                    pagerState.animateScrollToPage(effect.index)
                }

                is OnboardingEffect.RequestAudioPermission -> {
                    Timber.d("Onboarding effect RequestAudioPermission received")
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }

                is OnboardingEffect.NavigationToMain -> {
                    navController.navigate(Routes.STORAGE) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }

                is OnboardingEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.onIntent(OnboardingIntent.PagerChanged(pagerState.currentPage))
    }

    OnBoardingScreenUI(
        uiState = uiState,
        pagerState = pagerState,
        onIntent = { viewModel.onIntent(it) }
    )
}

@Composable
private fun OnBoardingScreenUI(
    uiState: OnboardingUiState,
    pagerState: PagerState,
    onIntent: (OnboardingIntent) -> Unit,
) {
    ChaGokBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 인디케이터는 항상 4개로 고정 (UI 일관성)
            PageIndicator(
                modifier = Modifier.padding(top = 96.dp),
                currentPage = uiState.currentPage,
                pageCount = ONBOARDING_PAGE_COUNT,
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = 105.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier,
                    // 핵심 수정 2: userScrollEnabled는 항상 true로 두어 스와이프를 허용함
                    userScrollEnabled = true
                ) { page ->
                    OnBoardingPage(
                        page = page,
                        selectedLanguage = uiState.selectedLanguage,
                        onLanguageChange = { onIntent(OnboardingIntent.SelectLanguage(it)) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val isLastPage = pagerState.currentPage == 3
                val buttonModifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                val buttonText = if (isLastPage) "시작하기" else "다음"

                if (isLastPage) {
                    ChaGokButton(
                        text = buttonText,
                        onClick = { onIntent(OnboardingIntent.ClickStart) },
                        modifier = buttonModifier,
                        enabled = !uiState.isLoading,
                        isLoading = uiState.isLoading
                    )
                } else {
                    ChaGokOutlinedButton(
                        text = buttonText,
                        onClick = { onIntent(OnboardingIntent.ClickNext) },
                        modifier = buttonModifier,
                        enabled = !uiState.isLoading,
                        isLoading = uiState.isLoading
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(82.dp)
                        .padding(bottom = 26.dp)
                ) {
                    when (uiState.currentPage) {
                        0 -> TextButton(onClick = { onIntent(OnboardingIntent.ClickSkip) }) {
                            Text(text = "건너뛰기", color = Color.Gray, style = ChaGokTextStyle.Body3)
                        }

                        1, 2 -> TextButton(onClick = { onIntent(OnboardingIntent.ClickBack) }) {
                            Text(text = "이전", color = Color.Gray, style = ChaGokTextStyle.Body3)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnBoardingPage(
    page: Int,
    selectedLanguage: Language,
    onLanguageChange: (Language) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = when (page) {
        0 -> "녹음부터 요약까지,\n내 기기에서 한 번에"
        1 -> "하루가 끝나면,\n기억은 먼저 정리돼버려요."
        2 -> "필요한 권한만\n요청할게요."
        else -> "기록할 언어를 선택해 주세요.."
    }

    val subTitle = when (page) {
        0 -> "서버 업로드 없이 저장되는\n프라이빗 기록"
        1 -> "놓치고 싶지 않은 말들이 있다면,\n내기기에 차곡차곡 기록하고 요약까지"
        2 -> "녹음을 시작하려면\n마이크 권한이 필요해요"
        else -> "텍스트 변환 정확도가 올라가요.\n언어는 나중에 변경할 수 있어요."
    }
    Box(modifier = modifier.padding(start = 20.dp)) {
        if (page == 3) {
            Column {
                Text(text = title, style = ChaGokTextStyle.Header1, color = TextPrimary)
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = subTitle, style = ChaGokTextStyle.Subtitle1, color = TextPrimary)
                Spacer(modifier = Modifier.height(36.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LanguageOption(
                        label = "한국어(기본설정)",
                        selected = selectedLanguage == Language.KOREAN,
                        onClick = { onLanguageChange(Language.KOREAN) }
                    )
                    LanguageOption(
                        label = "영어",
                        selected = selectedLanguage == Language.ENGLISH,
                        onClick = { onLanguageChange(Language.ENGLISH) }
                    )
                }

            }
        } else {
            Column {
                Text(text = title, style = ChaGokTextStyle.Header1, color = TextPrimary)
                Spacer(modifier = Modifier.height(32.dp))
                Text(text = subTitle, style = ChaGokTextStyle.Subtitle1, color = TextPrimary)
                Spacer(modifier = Modifier.height(36.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Image(
                        painter = painterResource(
                            id = when (page) {
                                0 -> R.drawable.on1
                                1 -> R.drawable.on2
                                else -> R.drawable.on3
                            }
                        ),
                        contentDescription = "온보딩 사진",
                        modifier = Modifier.padding(horizontal = 21.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
            if (selected) {
                // 선택되었을 때 흰색 배경 위에 올라가는 보라색 점
                Box(
                    modifier = Modifier
                        .size(12.dp) // 점 크기를 10dp 정도로 키우면 더 잘 보입니다
                        .background(PrimaryColor, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            style = ChaGokTextStyle.Subtitle1,
            // 선택 시 글자도 흰색으로 강조하면 더 예쁩니다 (피그마에 따라 조정)
            color = if (selected) Color.White else TextPrimary
        )
    }
}

@Composable
private fun PageIndicator(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(color = if (index == currentPage) TextPrimary else Gray400)
            )
        }
    }
}

// --- 상세 화면별 Preview ---

@Preview(showBackground = true, name = "1단계: 서비스 소개", device = "spec:width=360dp,height=800dp")
@Composable
fun Step1Preview() {

    OnBoardingScreenUI(
        uiState = OnboardingUiState(currentPage = 0),
        pagerState = rememberPagerState { ONBOARDING_PAGE_COUNT },
        onIntent = {}
    )
}

@Preview(showBackground = true, name = "2단계: 상세 안내", device = "spec:width=360dp,height=800dp")
@Composable
fun Step2Preview() {

    OnBoardingScreenUI(
        uiState = OnboardingUiState(currentPage = 1),
        pagerState = rememberPagerState { ONBOARDING_PAGE_COUNT },
        onIntent = {}
    )

}

@Preview(showBackground = true, name = "3단계: 권한 요청", device = "spec:width=360dp,height=800dp")
@Composable
fun Step3Preview() {

    OnBoardingScreenUI(
        uiState = OnboardingUiState(currentPage = 2, isPermissionGranted = false),
        pagerState = rememberPagerState { ONBOARDING_PAGE_COUNT },
        onIntent = {}
    )

}

@Preview(showBackground = true, name = "4단계: 언어 선택 (한국어 선택됨)", device = "spec:width=360dp,height=800dp")
@Composable
fun Step4KoreanPreview() {

    OnBoardingScreenUI(
        uiState = OnboardingUiState(
            currentPage = 3,
            selectedLanguage = Language.KOREAN
        ),
        pagerState = rememberPagerState(initialPage = 3) { ONBOARDING_PAGE_COUNT },
        onIntent = {}
    )

}

@Preview(showBackground = true, name = "4단계: 언어 선택 (영어 선택됨)", device = "spec:width=360dp,height=800dp")
@Composable
fun Step4EnglishPreview() {

    OnBoardingScreenUI(
        uiState = OnboardingUiState(
            currentPage = 3,
            selectedLanguage = Language.ENGLISH
        ),
        pagerState = rememberPagerState(initialPage = 3) { ONBOARDING_PAGE_COUNT },
        onIntent = {}
    )

}

@Composable
@Preview(showBackground = true, name = "인디케이터 컴포넌트")
private fun PageIndicatorStepPreview() {
    Column(
        modifier = Modifier
            .padding(20.dp)
            .background(Gray900)
    ) {
        PageIndicator(
            currentPage = 0,
            pageCount = 4
        )
    }
}