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
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
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
import com.roro.core.ui.theme.PrimaryColor
import com.roro.core.ui.theme.TextPrimary
import com.roro.onboarding.R
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

private const val ONBOARDING_PAGE_COUNT = 5

@Composable
fun OnBoardingScreen(navController: NavController) {
    val viewModel: OnBoardingViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val pagerState = rememberPagerState(pageCount = { ONBOARDING_PAGE_COUNT })

    BackHandler(enabled = true) {
        val isDownloading = uiState.modelDownloadState.gemma == DownloadItemState.Downloading
        val isChecking = uiState.isCheckingEnvironment
        if (isDownloading || (pagerState.currentPage == 3 && isChecking)) return@BackHandler

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
                    userScrollEnabled = uiState.modelDownloadState.gemma != DownloadItemState.Downloading
                ) { page ->
                    OnBoardingPage(
                        page = page,
                        selectedLanguage = uiState.selectedLanguage,
                        onLanguageChange = { onIntent(OnboardingIntent.SelectLanguage(it)) },
                        downloadState = uiState.modelDownloadState,
                        isDownloadStarted = uiState.isDownloadStarted,
                        isCheckingEnvironment = uiState.isCheckingEnvironment,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val isLastPage = pagerState.currentPage == 4
                val isDownloadPage = pagerState.currentPage == 3

                val buttonText = when {
                    isLastPage -> "시작하기"

                    uiState.modelDownloadState.gemma == DownloadItemState.Done ->
                        "다음"

                    uiState.isEnvironmentChecked &&
                            !uiState.isDownloadStarted ->
                        "다운로드"

                    uiState.isCheckingEnvironment ->
                        "환경 확인 중"

                    else ->
                        "다음"
                }

                val isButtonEnabled = when {
                    isDownloadPage -> when {
                        uiState.modelDownloadState.gemma == DownloadItemState.Done -> true
                        uiState.isCheckingEnvironment -> false  // 확인중
                        uiState.isDownloadStarted -> false       // 다운로드중
                        else -> true  // 초기 or 확인완료 → 버튼 활성
                    }
                    else -> uiState.isNextEnabled
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
                        enabled = isButtonEnabled,
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
                        0,3 -> TextButton(onClick = { onIntent(OnboardingIntent.ClickSkip) }) {
                            Text("건너뛰기", color = Color.Gray, style = ChaGokTextStyle.Body3)
                        }
                        1, 2, 4 -> TextButton(
                            onClick = { onIntent(OnboardingIntent.ClickBack) },
                            enabled = uiState.modelDownloadState.gemma != DownloadItemState.Downloading
                        ) {
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
    isCheckingEnvironment: Boolean,
    modifier: Modifier = Modifier,
) {
    val title = when (page) {
        0 -> "녹음부터 요약까지,\n내 기기에서 한 번에"
        1 -> "하루가 끝나면,\n기억은 먼저 정리돼버려요."
        2 -> "필요한 권한만\n요청할게요."
        3 -> "기기에서 바로 작동하도록\n몇 가지를 준비할게요."
        else -> "기록할 언어를 선택해 주세요."
    }

    Box(modifier = modifier.padding(start = 20.dp)) {
        when (page) {
            4 -> Column {
                Text(text = title, style = ChaGokTextStyle.Header1, color = TextPrimary)
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "텍스트 변환 정확도가 올라가요.\n언어는 나중에 변경할 수 있어요.",
                    style = ChaGokTextStyle.Subtitle1,
                    color = TextPrimary
                )
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

            3 -> Column {
                Text(
                    text = title,
                    style = ChaGokTextStyle.Header1,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(30.dp))

                AnimatedContent(
                    targetState = when {
                        isCheckingEnvironment || downloadState.isChecking -> "CHECKING"
                        downloadState.gemma == DownloadItemState.Downloading -> "DOWNLOADING"
                        downloadState.gemma == DownloadItemState.Done -> "DONE"
                        else -> "READY"
                    },
//                    transitionSpec = {
//                        slideInHorizontally(
//                            initialOffsetX = { it }
//                        ) + fadeIn() togetherWith fadeOut()
//                    },
                    transitionSpec = {
                        slideInHorizontally(
                            initialOffsetX = { -it }
                        ) + fadeIn() togetherWith

                                slideOutHorizontally(
                                    targetOffsetX = { +it / 3 }
                                ) + fadeOut()
                    },
                    label = "setup_content"
                ) { state ->

                    when (state) {

                        "CHECKING" -> {
                            Column {
                                Text(
                                    text = "모델 설치 유무와 기기 환경을\n확인중이에요...",
                                    style = ChaGokTextStyle.Subtitle1,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(40.dp))

                                ModelDownloadItem(
                                    label = "지원 환경 확인중",
                                    state = DownloadItemState.Checking
                                )
                            }
                        }

                        "READY" -> {
                            Column {
                                Text(
                                    text = "녹음과 요약을 기기 안에서 처리하기 위해\n필요한 모델을 다운로드해요.\nWi-Fi 연결을 권장하며 몇 분 정도 걸려요.",
                                    style = ChaGokTextStyle.Subtitle1,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(40.dp))

                                ModelDownloadItem(
                                    label = "Gemma-4",
                                    state = DownloadItemState.Required
                                )
                            }
                        }

                        "DOWNLOADING" -> {
                            Column {
                                Text(
                                    text = "모델을 다운로드하고 있어요.",
                                    style = ChaGokTextStyle.Subtitle1,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(40.dp))

                                ModelDownloadItem(
                                    label = "Gemma-4",
                                    state = DownloadItemState.Downloading,
                                    progress = downloadState.progress
                                )
                            }
                        }

                        else -> {
                            Column {
                                Text(
                                    text = "모델 준비가 완료되었어요.",
                                    style = ChaGokTextStyle.Subtitle1,
                                    color = TextPrimary
                                )

                                Spacer(modifier = Modifier.height(40.dp))

                                ModelDownloadItem(
                                    label = "Gemma-4",
                                    state = DownloadItemState.Done
                                )
                            }
                        }
                    }
                }
            }

            else -> Column {
                val subTitle = when (page) {
                    0 -> "서버 업로드 없이 저장되는\n프라이빗 기록"
                    1 -> "놓치고 싶지 않은 말들이 있다면,\n내 기기에 차곡차곡 기록하고 요약까지"
                    else -> "녹음을 시작하려면\n마이크 권한이 필요해요"
                }
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
) {
    val phase = when {
        downloadState.isChecking -> ModelDownloadPhase.CHECKING
        downloadState.gemma == DownloadItemState.Unavailable -> ModelDownloadPhase.UNAVAILABLE
        downloadState.gemma == DownloadItemState.Failed -> ModelDownloadPhase.UNAVAILABLE
        downloadState.gemma == DownloadItemState.Downloading -> ModelDownloadPhase.DOWNLOADING
        downloadState.gemma == DownloadItemState.Done -> ModelDownloadPhase.DONE
        downloadState.gemma == DownloadItemState.Idle -> ModelDownloadPhase.IDLE
        else -> ModelDownloadPhase.READY
    }

    val showCheckItem = phase == ModelDownloadPhase.CHECKING || phase == ModelDownloadPhase.UNAVAILABLE

    AnimatedContent(
        targetState = showCheckItem,
        transitionSpec = {
            if (targetState) {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(400, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f))
                ) + fadeIn(tween(300)) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(400, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f))
                        ) + fadeOut(tween(300))
            } else {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(400, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f))
                ) + fadeIn(tween(300)) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = tween(400, easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f))
                        ) + fadeOut(tween(300))
            }
        },
        label = "downloadItem",
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) { isShowingCheckItem ->
        if (isShowingCheckItem) {
            val isUnavailable = phase == ModelDownloadPhase.UNAVAILABLE
            ModelDownloadItem(
                label = if (isUnavailable) "지원되지 않는 기기입니다" else "지원 환경 확인중",
                state = if (isUnavailable) DownloadItemState.Failed else DownloadItemState.Checking,
            )
        } else {
            ModelDownloadItem(
                label = "Gemma-4",
                state = when (phase) {
                    ModelDownloadPhase.IDLE -> DownloadItemState.Idle
                    ModelDownloadPhase.READY -> DownloadItemState.Required
                    ModelDownloadPhase.DOWNLOADING -> DownloadItemState.Downloading
                    ModelDownloadPhase.DONE -> DownloadItemState.Done
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
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(12.dp))
            //.defaultMinSize(minHeight = 72.dp)
            .padding(horizontal = 20.dp, vertical = 14.dp),

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
                DownloadItemState.Idle -> CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White.copy(alpha = 0.3f),
                    strokeWidth = 2.dp
                )
                DownloadItemState.Checking -> CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = PrimaryColor,
                    strokeWidth = 2.dp
                )
                DownloadItemState.Downloading ->
                    if (progress > 0f) {
                        Text("${(progress * 100).toInt()}%", color = PrimaryColor, style = ChaGokTextStyle.Body3)
                    } else {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = PrimaryColor, strokeWidth = 2.dp)
                    }
                DownloadItemState.Done -> Icon(Icons.Default.Check, null, tint = PrimaryColor, modifier = Modifier.size(18.dp))
                DownloadItemState.Required -> Text("", color = Color.Red, style = ChaGokTextStyle.Body3)
                DownloadItemState.Unavailable -> Text("미지원", color = Gray400, style = ChaGokTextStyle.Body3)
                DownloadItemState.Failed -> Icon(Icons.Default.Error, null, tint = Color.Red, modifier = Modifier.size(18.dp))
            }
        }
        if (state == DownloadItemState.Downloading && progress > 0f) {
            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(15.dp))

            Text(
                text = "다운로드 중입니다..",
                style = ChaGokTextStyle.Body3,
                color = Color.White.copy(alpha = 0.5f)
            )


        }
    }
}

@Composable
private fun LanguageOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.size(20.dp).background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) Box(modifier = Modifier.size(12.dp).background(PrimaryColor, CircleShape))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, style = ChaGokTextStyle.Subtitle1, color = if (selected) Color.White else TextPrimary)
    }
}

@Composable
private fun PageIndicator(currentPage: Int, pageCount: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(2.dp)
                    .background(if (index == currentPage) TextPrimary else Gray400)
            )
        }
    }
}

// Preview들
@Preview(showBackground = true, name = "① 초기", device = "spec:width=360dp,height=800dp")
@Composable
fun DownloadIdlePreview() {
    ChaGokBackground {
        Box(modifier = Modifier.fillMaxSize().padding(top = 200.dp)) {
            OnBoardingPage(
                page = 3, selectedLanguage = Language.KOREAN, onLanguageChange = {},
                downloadState = ModelDownloadState(isChecking = false),
                isDownloadStarted = false, isCheckingEnvironment = false,
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
                page = 3, selectedLanguage = Language.KOREAN, onLanguageChange = {},
                downloadState = ModelDownloadState(isChecking = true),
                isDownloadStarted = false, isCheckingEnvironment = true,
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
                page = 3, selectedLanguage = Language.KOREAN, onLanguageChange = {},
                downloadState = ModelDownloadState(isChecking = false, gemma = DownloadItemState.Downloading, progress = 0.47f),
                isDownloadStarted = true, isCheckingEnvironment = false,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true, name = "④ 완료", device = "spec:width=360dp,height=800dp")
@Composable
fun DownloadDonePreview() {
    ChaGokBackground {
        Box(modifier = Modifier.fillMaxSize().padding(top = 200.dp)) {
            OnBoardingPage(
                page = 3, selectedLanguage = Language.KOREAN, onLanguageChange = {},
                downloadState = ModelDownloadState(isChecking = false, gemma = DownloadItemState.Done, progress = 1f),
                isDownloadStarted = true, isCheckingEnvironment = false,
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
                page = 3, selectedLanguage = Language.KOREAN, onLanguageChange = {},
                downloadState = ModelDownloadState(isChecking = false, gemma = DownloadItemState.Failed),
                isDownloadStarted = true, isCheckingEnvironment = false,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(showBackground = true, name = "미지원", device = "spec:width=360dp,height=800dp")
@Composable
fun DownloadUnavailablePreview() {
    ChaGokBackground {
        Box(modifier = Modifier.fillMaxSize().padding(top = 200.dp)) {
            OnBoardingPage(
                page = 3, selectedLanguage = Language.KOREAN, onLanguageChange = {},
                downloadState = ModelDownloadState(isChecking = false, gemma = DownloadItemState.Unavailable),
                isDownloadStarted = true, isCheckingEnvironment = false,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

enum class ModelDownloadPhase {
    IDLE, CHECKING, READY, DOWNLOADING, DONE, UNAVAILABLE
}

enum class SetupStep {
    CHECKING,
    READY_TO_DOWNLOAD,
    DOWNLOADING,
    DONE,
    UNSUPPORTED
}