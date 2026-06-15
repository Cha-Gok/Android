package com.roro.chagok.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.roro.chagok.splash.navigation.splashGraph
import com.roro.core.navigation.Routes
import com.roro.onboarding.navigation.onBoardingGraph
import com.roro.recorder.navigation.recorderGraph
import com.roro.recorder.presentation.RecordViewModel
import com.roro.recorder.presentation.uiState.RecordState

import com.roro.storage.navigation.storageGraph
import com.roro.storage.navigation.trashGraph

/**
 * 기능 설명:
 * - 앱 전체의 네비게이션 그래프를 연결하는 루트 NavHost.
 * - feature 모듈의 navigation graph들을 여기서 등록한다.
 *
 * startDestination:
 * - 앱 시작 시 처음 보여줄 화면 (현재는 파일관리 화면)
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
) {

    // 스켈레톤 UI
    val recordViewModel: RecordViewModel = hiltViewModel()
    val recordState by recordViewModel.state.collectAsStateWithLifecycle()

    // ✅ state 변화 감지해서 네비게이션
    LaunchedEffect(recordState) {
        when (val s = recordState) {
            is RecordState.Processing ->
                navController.navigate(Routes.RECORD_RESULT_WAITING)

            is RecordState.Success ->
                navController.navigate(Routes.recordResult(s.voiceNoteId)) {
                    popUpTo(Routes.RECORDER) { inclusive = true }
                }
            is RecordState.NoSpeech ->
                navController.navigate(Routes.recordResult(s.voiceNoteId)) {
                    popUpTo(Routes.RECORDER) { inclusive = true }
                }
            is RecordState.SummaryError ->
                navController.navigate(Routes.recordResult(s.voiceNoteId)) {
                    popUpTo(Routes.RECORDER) { inclusive = true }
                }
            else -> {}
        }
    }


    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
    ) {
        // 스플래시 그래프
        splashGraph(
            onNavigateToMain = {

                // 기존 코드
                navController.navigate(Routes.STORAGE) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }

            },
            onNavigateToOnboarding = {
                navController.navigate(Routes.ONBOARDING) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            },
        )

        onBoardingGraph(navController = navController)

        // onStartRecord 콜백 전달
        storageGraph(
            navController = navController,
        )

        recorderGraph(
            navController = navController,
            recordViewModel = recordViewModel  // ✅ 전달
        )

        trashGraph(
            navController = navController
        )
    }

}