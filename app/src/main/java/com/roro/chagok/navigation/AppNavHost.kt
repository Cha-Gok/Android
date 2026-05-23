package com.roro.chagok.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.roro.chagok.splash.navigation.splashGraph
import com.roro.core.navigation.Routes
import com.roro.onboarding.navigation.onBoardingGraph
import com.roro.recorder.navigation.recorderGraph
import com.roro.recorder.presentation.RecordViewModel
import com.roro.recorder.presentation.screen.RecorderBottomSheet
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

    // 바텀 시트 상태 app 레이어에서 관리
    var showRecorder by remember { mutableStateOf(false) }

    // 스켈레톤 UI
    val recordViewModel: RecordViewModel = hiltViewModel()

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
            onStartRecord = { showRecorder = true }
        )

        recorderGraph(
            navController = navController,
            recordViewModel = recordViewModel  // ✅ 전달
        )
    }

    // 바텀 시트 — app 레이어에서 recorder 모듈 직접 호출
    if (showRecorder) {
        RecorderBottomSheet(
            navController = navController,
            onDismiss = { showRecorder = false },
            viewModel = recordViewModel  // ✅ 동일 인스턴스 전달
        )
    }
}