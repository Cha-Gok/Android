package com.roro.chagok.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.roro.chagok.splash.navigation.splashGraph
import com.roro.core.navigation.Routes
import com.roro.onboarding.navigation.onBoardingGraph
import com.roro.recorder.navigation.recorderGraph
import com.roro.storage.navigation.storageGraph

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
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
    ) {
        // 스플래시 그래프
        splashGraph(
            onNavigateToMain = {

                // 테스트용 수정
                navController.navigate(Routes.RECORDER) {  // STORAGE → RECORDER
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }

                // 기존 코드
//                navController.navigate(Routes.STORAGE) {
//                    popUpTo(Routes.SPLASH) { inclusive = true }
//                }

            },
            onNavigateToOnboarding = {
                navController.navigate(Routes.ONBOARDING) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            },
        )
        onBoardingGraph(navController = navController)
        storageGraph(navController = navController)
        recorderGraph(navController = navController)
    }
}