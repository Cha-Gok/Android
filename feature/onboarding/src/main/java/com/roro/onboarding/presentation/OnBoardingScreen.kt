// OnBoardingScreen.kt
package com.roro.onboarding.presentation

import android.Manifest
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

import androidx.compose.animation.slideOutHorizontally

private const val ONBOARDING_PAGE_COUNT = 6

@Composable
fun OnBoardingScreen(navController: NavController) {
    val viewModel: OnBoardingViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val pagerState = rememberPagerState(pageCount = { ONBOARDING_PAGE_COUNT })

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
                is OnboardingEffect.ScrollToPage ->
                    pagerState.animateScrollToPage(effect.index)
                is OnboardingEffect.RequestAudioPermission ->
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                is OnboardingEffect.NavigationToMain ->
                    navController.navigate(Routes.STORAGE) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                is OnboardingEffect.ShowToast ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
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
            PageIndicator(
                modifier = Modifier.padding(top = 70.dp),
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
                    userScrollEnabled = true
                ) { page ->
                    OnBoardingPage(
                        page = page,
                        selectedLanguage = uiState.selectedLanguage,
                        onLanguageChange = { onIntent(OnboardingIntent.SelectLanguage(it)) },
                        downloadState = uiState.modelDownloadState,
                        isDownloadStarted = uiState.isDownloadStarted,
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
                val isLastPage = pagerState.currentPage == 5
                val isDownloadPage = pagerState.currentPage == 4

                val buttonText = when {
                    isLastPage -> "시작하기"
                    isDownloadPage && uiState.isDownloadStarted -> "다음"
                    isDownloadPage -> "다운로드"
                    else -> "다음"
                }

                val buttonModifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)

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
                        enabled = uiState.isNextEnabled,
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
                            Text("건너뛰기", color = Color.Gray, style = ChaGokTextStyle.Body3)
                        }
                        1, 2, 3,4 -> TextButton(onClick = { onIntent(OnboardingIntent.ClickBack) }) {
                            Text("이전", color = Color.Gray, style = ChaGokTextStyle.Body3)
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
    downloadState: ModelDownloadState,
    isDownloadStarted: Boolean,
    modifier: Modifier = Modifier,
) {
    val title = when (page) {
        0 -> "녹음부터 요약까지,\n내 기기에서 한 번에"
        1 -> "하루가 끝나면,\n기억은 먼저 정리돼버려요."
        2 -> "필요한 권한만\n요청할게요."
        3 -> "기기에서 바로 작동하도록\n몇 가지를 준비할게요."
        4 -> "기기에서 바로 작동하도록\n몇 가지를 준비할게요."
        else -> "기록할 언어를 선택해 주세요."
    }

    val subTitle = when (page) {
        0 -> "서버 업로드 없이 저장되는\n프라이빗 기록"
        1 -> "놓치고 싶지 않은 말들이 있다면,\n내기기에 차곡차곡 기록하고 요약까지"
        2 -> "녹음을 시작하려면\n마이크 권한이 필요해요"
        3 -> "모델 설치 유무와 기기 환경을\n확인중이에요..."
        4 -> "녹음과 요약을 기기 안에서 처리하기 위해\n필요한 모델을 다운로드해요.\nWi-Fi 연결을 권장하며 몇 분 정도 걸려요."
        else -> "텍스트 변환 정확도가 올라가요.\n언어는 나중에 변경할 수 있어요."
    }

    Box(modifier = modifier.padding(start = 20.dp)) {
        when (page) {
            5 -> Column {
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

            4 -> Column {
                Text(text = title, style = ChaGokTextStyle.Header1, color = TextPrimary)
                Spacer(modifier = Modifier.height(20.dp))  // ← 추가하면 아래로 밀림
                // Gemma-4 다운로드 아이템
                ModelDownloadItem(
                    label = "Gemma-4",
                    state = when {
                        isDownloadStarted -> downloadState.gemma
                        else -> DownloadItemState.Idle
                    },
                    progress = downloadState.progress
                )
            }
            3 -> Column {
                Text(text = title, style = ChaGokTextStyle.Header1, color = TextPrimary)
                Spacer(modifier = Modifier.height(20.dp))  // ← 추가하면 아래로 밀림
                // 지원 환경 확인 아이템만
                ModelDownloadItem(
                    label = if (downloadState.isChecking) "지원 환경 확인중" else "지원 환경 확인 완료",
                    state = if (downloadState.isChecking) DownloadItemState.Checking else DownloadItemState.Done
                )
            }

            else -> Column {
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
private fun ModelDownloadContent(
    downloadState: ModelDownloadState,
    isDownloadStarted: Boolean
) {
    val isChecking = downloadState.isChecking
    val gemma = downloadState.gemma
    val showCheckItem = isChecking || gemma == DownloadItemState.Done
    val showGemmaItem = !showCheckItem

    Box {
        // 지원 환경 확인 아이템 - 왼쪽에서 슬라이드 인/아웃
        AnimatedVisibility(
            visible = showCheckItem,
            enter = slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(400, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f))
            ) + fadeIn(tween(300)),
            exit = slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(400, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f))
            ) + fadeOut(tween(300))
        ) {
            ModelDownloadItem(
                label = if (isChecking) "지원 환경 확인중" else "지원 환경 확인 완료",
                state = if (isChecking) DownloadItemState.Checking else DownloadItemState.Done
            )
        }

        // Gemma-4 아이템 - 오른쪽에서 슬라이드 인
        AnimatedVisibility(
            visible = showGemmaItem,
            enter = slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(400, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f))
            ) + fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            ModelDownloadItem(
                label = "Gemma-4",
                state = when {
                    isDownloadStarted -> gemma
                    else -> DownloadItemState.Idle
                },
                progress = downloadState.progress
            )
        }
    }
}

@Composable
private fun ModelDownloadItem(
    label: String,
    state: DownloadItemState,
    progress: Float = 0f
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 20.dp)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when (state) {
                    DownloadItemState.Done -> Icons.Default.CheckCircle
                    DownloadItemState.Failed -> Icons.Default.Error
                    else -> Icons.Default.Storage
                },
                contentDescription = null,
                tint = when (state) {
                    DownloadItemState.Done -> PrimaryColor
                    DownloadItemState.Failed -> Color.Red
                    else -> Color.White.copy(alpha = 0.5f)
                },
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = label,
                style = ChaGokTextStyle.Subtitle1,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )

            when (state) {
                DownloadItemState.Idle ->
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White.copy(alpha = 0.3f),
                        strokeWidth = 2.dp
                    )
                DownloadItemState.Checking ->
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = PrimaryColor,
                        strokeWidth = 2.dp
                    )
                DownloadItemState.Downloading ->
                    if (progress > 0f) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            color = PrimaryColor,
                            style = ChaGokTextStyle.Body3
                        )
                    } else {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = PrimaryColor,
                            strokeWidth = 2.dp
                        )
                    }
                DownloadItemState.Done ->
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                DownloadItemState.Required ->
                    Text("다운로드 필요", color = Color.Red, style = ChaGokTextStyle.Body3)
                DownloadItemState.Unavailable ->
                    Text("미지원", color = Gray400, style = ChaGokTextStyle.Body3)
                DownloadItemState.Failed ->
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(18.dp)
                    )
            }
        }

        // 다운로드 중 진행률 바
        if (state == DownloadItemState.Downloading && progress > 0f) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryColor,
                trackColor = Color.White.copy(alpha = 0.1f)
            )
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
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(color = Color.White, shape = CircleShape),
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
            style = ChaGokTextStyle.Subtitle1,
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


@Preview(showBackground = true, name = "① 초기 진입", device = "spec:width=360dp,height=800dp")
@Composable
fun DownloadIdlePreview() {
    ChaGokBackground {
        Box(modifier = Modifier.fillMaxSize().padding(top = 200.dp)) {
            OnBoardingPage(
                page = 3,
                selectedLanguage = Language.KOREAN,
                onLanguageChange = {},
                downloadState = ModelDownloadState(isChecking = false),
                isDownloadStarted = false,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true, name = "② 환경 체크 중", device = "spec:width=360dp,height=800dp")
@Composable
fun DownloadCheckingPreview() {
    ChaGokBackground {
        Box(modifier = Modifier.fillMaxSize().padding(top = 200.dp)) {
            OnBoardingPage(
                page = 3,
                selectedLanguage = Language.KOREAN,
                onLanguageChange = {},
                downloadState = ModelDownloadState(isChecking = true),
                isDownloadStarted = false,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true, name = "③ 다운로드 중 (47%)", device = "spec:width=360dp,height=800dp")
@Composable
fun DownloadingPreview() {
    ChaGokBackground {
        Box(modifier = Modifier.fillMaxSize().padding(top = 200.dp)) {
            OnBoardingPage(
                page = 3,
                selectedLanguage = Language.KOREAN,
                onLanguageChange = {},
                downloadState = ModelDownloadState(
                    isChecking = false,
                    gemma = DownloadItemState.Downloading,
                    progress = 0.47f
                ),
                isDownloadStarted = true,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true, name = "④ 다운로드 완료", device = "spec:width=360dp,height=800dp")
@Composable
fun DownloadDonePreview() {
    ChaGokBackground {
        Box(modifier = Modifier.fillMaxSize().padding(top = 200.dp)) {
            OnBoardingPage(
                page = 3,
                selectedLanguage = Language.KOREAN,
                onLanguageChange = {},
                downloadState = ModelDownloadState(
                    isChecking = false,
                    gemma = DownloadItemState.Done,
                    progress = 1f
                ),
                isDownloadStarted = true,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true, name = "⑤ 실패", device = "spec:width=360dp,height=800dp")
@Composable
fun DownloadFailedPreview() {
    ChaGokBackground {
        Box(modifier = Modifier.fillMaxSize().padding(top = 200.dp)) {
            OnBoardingPage(
                page = 3,
                selectedLanguage = Language.KOREAN,
                onLanguageChange = {},
                downloadState = ModelDownloadState(
                    isChecking = false,
                    gemma = DownloadItemState.Failed
                ),
                isDownloadStarted = true,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true, name = "미지원 기기", device = "spec:width=360dp,height=800dp")
@Composable
fun DownloadUnavailablePreview() {
    ChaGokBackground {
        Box(modifier = Modifier.fillMaxSize().padding(top = 200.dp)) {
            OnBoardingPage(
                page = 3,
                selectedLanguage = Language.KOREAN,
                onLanguageChange = {},
                downloadState = ModelDownloadState(
                    isChecking = false,
                    gemma = DownloadItemState.Unavailable
                ),
                isDownloadStarted = true,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}