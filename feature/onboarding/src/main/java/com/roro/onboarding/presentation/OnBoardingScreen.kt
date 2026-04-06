package com.roro.onboarding.presentation

import android.Manifest
import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.roro.core.datastore.Language
import com.roro.core.navigation.Routes
import com.roro.core.ui.theme.ChaGokTheme
import kotlinx.coroutines.flow.collect
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
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 인디케이터는 항상 4개로 고정 (UI 일관성)
            PageIndicator(
                currentPage = uiState.currentPage,
                pageCount = ONBOARDING_PAGE_COUNT,
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                // 핵심 수정 2: userScrollEnabled는 항상 true로 두어 스와이프를 허용함
                userScrollEnabled = true
            ) { page ->
                OnBoardingPage(
                    page = page,
                    selectedLanguage = uiState.selectedLanguage,
                    onLanguageChange = { onIntent(OnboardingIntent.SelectLanguage(it)) },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        when {
                            uiState.isLastPage -> onIntent(OnboardingIntent.ClickStart)
                            uiState.currentPage == 2 && !uiState.isPermissionGranted -> {
                                onIntent(OnboardingIntent.ClickPermissionRequest)
                            }

                            else -> onIntent(OnboardingIntent.ClickNext)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = if (uiState.isLastPage) "시작하기" else "다음")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier.height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (uiState.currentPage) {
                        0 -> {
                            TextButton(onClick = { onIntent(OnboardingIntent.ClickSkip) }) {
                                Text(text = "건너뛰기", color = Color.Gray)
                            }
                        }

                        1, 2 -> {
                            TextButton(onClick = { onIntent(OnboardingIntent.ClickBack) }) {
                                Text(text = "이전", color = Color.Gray)
                            }
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
        0 -> "환영합니다!\n차곡차곡 데이터를 관리해보세요."
        1 -> "모든 녹음 내용은\n자동으로 정리됩니다."
        2 -> "원활한 녹음을 위해\n마이크 권한이 필요해요."
        else -> "사용하실 언어를\n선택해주세요."
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (page == 3) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = title, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(32.dp))
                LanguageOption(
                    label = "한국어",
                    selected = selectedLanguage == Language.KOREAN,
                    onClick = { onLanguageChange(Language.KOREAN) }
                )
                LanguageOption(
                    label = "English",
                    selected = selectedLanguage == Language.ENGLISH,
                    onClick = { onLanguageChange(Language.ENGLISH) }
                )
            }
        } else {
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun LanguageOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun PageIndicator(currentPage: Int, pageCount: Int) {
    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = if (index == currentPage) 24.dp else 8.dp, height = 8.dp)
                    .clip(MaterialTheme.shapes.small)
                    .size(8.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (index == currentPage) MaterialTheme.colorScheme.primary else Color.LightGray,
                    shape = MaterialTheme.shapes.small,
                    content = {}
                )
            }
        }
    }
}

// --- Preview ---
@Preview(showBackground = true, name = "Step 1: Welcome")
@Composable
fun Step1Preview() = OnBoardingStepPreview(0)

@Preview(showBackground = true, name = "Step 2: Guide")
@Composable
fun Step2Preview() = OnBoardingStepPreview(1)

@Preview(showBackground = true, name = "Step 3: Permission")
@Composable
fun Step3Preview() = OnBoardingStepPreview(2)

@Preview(showBackground = true, name = "Step 4: Language")
@Composable
fun Step4Preview() = OnBoardingStepPreview(3)

@Composable
private fun OnBoardingStepPreview(page: Int) {
    ChaGokTheme {
        OnBoardingScreenUI(
            uiState = OnboardingUiState(currentPage = page),
            pagerState = rememberPagerState { ONBOARDING_PAGE_COUNT },
            onIntent = {}
        )
    }
}