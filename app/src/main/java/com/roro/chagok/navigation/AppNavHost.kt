package com.roro.chagok.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.roro.core.navigation.Routes
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
){
    NavHost(
        navController = navController,
        startDestination = Routes.STORAGE
    ) {
        // 파일 저장소 관련 화면 그래프
        storageGraph(navController = navController)
        // 녹음 기능 관련 화면 그래프
        recorderGraph(navController = navController)
    }
}