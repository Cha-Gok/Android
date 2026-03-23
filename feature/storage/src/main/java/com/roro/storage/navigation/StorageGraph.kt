package com.roro.storage.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.roro.core.navigation.Routes
import com.roro.storage.presentation.StorageDetailScreen
import com.roro.storage.presentation.StorageScreen

/**
 * 기능 설명:
 * 파일 기능과 관련된 화면들의 네비게이션을 정의한다.
 * app 모듈의 MainNavHost에서 호출되어 그래프에 등록한다.
 *
 * 포함 된 화면
 * 1. StorageScreen
 *    - 파일관리 메인 화면
 *    - 바텀 네비게이션에 포함 되는 화면
 * 2. 추후 입력...
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
fun NavGraphBuilder.storageGraph(
    navController: NavController
) {
    // 바텀 네비게이션 O
    composable(Routes.STORAGE) {
        StorageScreen(navController = navController)
    }

    // 바텀 네비게이션 X
    composable(Routes.STORAGE_DETAIL) { backStackEntry ->
        val fileId = backStackEntry.arguments?.getString("folderId").orEmpty()
        StorageDetailScreen(navController = navController, folderId = fileId)
    }
}