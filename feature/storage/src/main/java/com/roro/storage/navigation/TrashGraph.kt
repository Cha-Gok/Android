package com.roro.storage.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.roro.core.navigation.Routes
import com.roro.core.navigation.SearchType
import com.roro.recorder.presentation.screen.TrashRecordResultScreen
import com.roro.storage.presentation.filelist.FileListScreen
import com.roro.storage.presentation.search.SearchScreen

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
fun NavGraphBuilder.trashGraph(
    navController: NavController
) {
    val animationDuration = 500

    composable(
        Routes.SEARCH,
        arguments = listOf(navArgument("searchType") { type = NavType.StringType }),
        enterTransition = {
            // 1번 -> 2번으로 올 때: 왼쪽으로 밀면서 들어옴
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(animationDuration))
        },
        exitTransition = {
            // 2번 -> 3번으로 갈 때: 왼쪽으로 밀면서 나감 (이부분이 수정됨)
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(animationDuration))
        },
        popEnterTransition = {
            // 3번 -> 2번으로 돌아올 때: 오른쪽으로 밀면서 들어옴 (추가)
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(animationDuration))
        },
        popExitTransition = {
            // 2번 -> 1번으로 돌아갈 때: 오른쪽으로 밀면서 나감 (추가)
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(animationDuration))
        }
    ) { backStackEntry ->
        val searchType = backStackEntry.arguments?.getString("searchType") ?: SearchType.TRASH.name

        SearchScreen(
            searchType = searchType,
            navController = navController
        )
    }

    // 폴더 상세
    composable(
        route = Routes.STORAGE_FILE,
        arguments = listOf(
            navArgument("folderId") { type = NavType.StringType },
            navArgument("folderName") { type = NavType.StringType },
            navArgument("isTrash") {
                type = NavType.BoolType
                defaultValue = true
            }
        ),
        enterTransition = {
            // 2번 -> 3번으로 올 때: 왼쪽으로 밀면서 들어옴
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(animationDuration))
        },
        exitTransition = {
            // 3번 -> 4번(있다면)으로 갈 때: 왼쪽으로 밀면서 나감
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(animationDuration))
        },
        popEnterTransition = {
            // 4번 -> 3번으로 돌아올 때: 오른쪽으로 밀면서 들어옴
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(animationDuration))
        },
        popExitTransition = {
            // 3번 -> 2번으로 돌아갈 때: 오른쪽으로 밀면서 나감
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(animationDuration))
        }
    ) { backStackEntry ->
        val folderId = backStackEntry.arguments?.getString("folderId").orEmpty()
        val folderName = backStackEntry.arguments?.getString("folderName").orEmpty()
        val isTrash = backStackEntry.arguments?.getBoolean("isTrash") ?: false

        FileListScreen(
            navController = navController,
            folderName = folderName, folderId = folderId,
            isTrash = isTrash
        )
    }

    // ✅ 2. 휴지통 상세 화면 추가
    composable(
        route = Routes.TRASH_VOICE_NOTE,
        arguments = listOf(
            navArgument("voiceNoteId") { type = NavType.StringType }
        ),
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(animationDuration))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(animationDuration))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(animationDuration))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(animationDuration))
        }
    ) { backStackEntry ->
        val voiceNoteId = backStackEntry.arguments?.getString("voiceNoteId") ?: ""

        // 💡 여기에 실제 상세 화면 Composable을 연결하세요.
        // 예: TrashDetailScreen(voiceNoteId = voiceNoteId, navController = navController)
        // 지금은 화면이 없을 수 있으니 임시로 연결해두겠습니다.
        TrashRecordResultScreen(
            navController = navController,
            voiceNoteId = voiceNoteId,
        )

    }
}