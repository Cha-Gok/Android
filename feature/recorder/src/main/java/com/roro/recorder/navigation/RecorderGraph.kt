package com.roro.recorder.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.roro.core.navigation.Routes
import com.roro.recorder.presentation.screen.RecorderDetailScreen
import com.roro.recorder.presentation.screen.RecorderScreen

/**
 * 기능 설명:
 * 녹음 기능과 관련된 화면들의 네비게이션을 정의한다.
 * app 모듈의 MainNavHost에서 호출되어 그래프에 등록한다.
 *
 * 포함 된 화면
 * 1. RecorderScreen
 *    - 녹음 메인 화면
 *    - 바텀 네비게이션에 포함 되는 화면
 * 2. 추후 입력...
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
fun NavGraphBuilder.recorderGraph(
    navController: NavController
){
    // 바텀 네비게이션 O
    composable(Routes.RECORDER) {
        RecorderScreen(navController = navController)
    }

    // 바텀 네비게이션 X
    composable(Routes.RECORD_DETAIL) { backStackEntry ->
        val fileId = backStackEntry.arguments?.getString("fileId").orEmpty()
        RecorderDetailScreen(navController = navController,fileId =fileId)
    }
}