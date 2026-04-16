package com.roro.chagok.splash.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.roro.core.R
import com.roro.core.ui.component.ChaGokBackground
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
    ChaGokBackground {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.components),
                tint = Color.Unspecified,
                contentDescription = "logo",
            )
            Spacer(modifier = Modifier.height(33.dp))
            Text(
                text = "차곡",
                color = Color(0xFFDDDDDD),
                fontSize = 60.sp,
            )
        }

    }
}


@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    SplashScreenContent(
        uiState = SplashUiState(isLoading = true)
    )
}