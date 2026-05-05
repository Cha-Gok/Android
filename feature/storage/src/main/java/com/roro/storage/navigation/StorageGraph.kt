package com.roro.storage.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.roro.core.navigation.Routes
import com.roro.storage.presentation.FileListScreen
import com.roro.storage.presentation.PrivateFolderScreen
import com.roro.storage.presentation.home.StorageScreen
import com.roro.storage.presentation.tos.TosScreen
import com.roro.storage.presentation.trash.TrashScreen

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
    val animationDuration = 500

    // 바텀 네비게이션 O
    composable(Routes.STORAGE) {
        StorageScreen(navController = navController)
    }

    // 개인 폴더
    composable(
        Routes.STORAGE_FOLDER,
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
    ) {
        PrivateFolderScreen(navController = navController)
    }


    // 폴더 상세
    composable(
        route = Routes.STORAGE_FILE,
        arguments = listOf(
            navArgument("folderId") { type = NavType.StringType },
            navArgument("folderName") { type = NavType.StringType }
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
        FileListScreen(navController = navController, folderName = folderName, folderId = folderId)
    }

    // 휴지통
    // 개인 폴더
    composable(
        Routes.TRASH,
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
    ) {
        TrashScreen(navController = navController)
    }

    // 이용약관
    // 휴지통
    // 개인 폴더
    composable(
        Routes.TOS,
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
    ) {
        TosScreen(navController = navController)
    }

}