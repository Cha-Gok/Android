package com.roro.chagok.splash.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.roro.core.ui.theme.ChaGokTheme
import timber.log.Timber

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Timber.d("스플래시 화면 데이터 로딩 중...")
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SplashEffect.NavigationToMain -> onNavigateToMain()
                SplashEffect.NavigationToOnboarding -> onNavigateToOnboarding()
            }
        }
    }

    SplashScreenContent(uiState = uiState)
}

@Composable
internal fun SplashScreenContent(
    uiState: SplashUiState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "ChaGok",
            color = Color.White,
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    ChaGokTheme {
        SplashScreenContent(
            uiState = SplashUiState(isLoading = true)
        )
    }
}