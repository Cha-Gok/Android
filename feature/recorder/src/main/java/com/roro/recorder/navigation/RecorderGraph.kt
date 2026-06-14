package com.roro.recorder.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.roro.core.navigation.Routes
import com.roro.recorder.presentation.RecordViewModel
import com.roro.recorder.presentation.screen.RecordResultScreen
import com.roro.recorder.presentation.screen.RecorderDetailScreen
import com.roro.recorder.presentation.screen.ScriptEditScreen
import com.roro.recorder.presentation.screen.SearchResultScreen
import com.roro.recorder.presentation.viewModel.RecordResultViewModel
import androidx.compose.animation.core.tween
import com.roro.recorder.presentation.screen.RecordResultLoadingScreen

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
    navController: NavController,
    recordViewModel: RecordViewModel
) {
    // 녹음 화면
    composable(
        Routes.RECORDER,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Up,
                tween(400)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Down,
                tween(400)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Up,
                tween(400)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Down,
                tween(400)
            )
        }
    ) {
        RecorderDetailScreen(
            navController = navController,
            viewModel = recordViewModel
        )
    }

    // 녹음 결과 화면
    composable(Routes.RECORD_RESULT) { backStackEntry ->
        val voiceNoteId = backStackEntry.arguments?.getString("voiceNoteId").orEmpty()
        RecordResultScreen(navController = navController, voiceNoteId = voiceNoteId)
    }


//    // 처리 중 스켈레톤 화면
//    composable(Routes.RECORD_RESULT_WAITING) {
//        RecordResultScreen(
//            navController = navController,
//            voiceNoteId = "",
//            recordViewModel = recordViewModel
//        )
//    }

    // 처리 중 스켈레톤 화면 - 그냥 스켈레톤만 표시
    composable(Routes.RECORD_RESULT_WAITING) {
        RecordResultLoadingScreen()   // ✅ recordViewModel 필요 없음
    }

    // 스크립트 편집 화면 ✅
    composable(Routes.SCRIPT_EDIT) { backStackEntry ->
        val voiceNoteId = backStackEntry.arguments?.getString("voiceNoteId").orEmpty()

        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry(Routes.recordResult(voiceNoteId))
        }
        val recordResultViewModel: RecordResultViewModel = hiltViewModel(parentEntry)

        ScriptEditScreen(
            navController = navController,
            voiceNoteId = voiceNoteId,
            onScriptSaved = { recordResultViewModel.onScriptSaved() }
        )
    }

    // 검색 화면
    // 검색 화면
    composable(Routes.SEARCH) { backStackEntry ->
        val voiceNoteId = backStackEntry.arguments?.getString("voiceNoteId").orEmpty()

        val parentEntry = remember(backStackEntry) {
            navController.getBackStackEntry(Routes.recordResult(voiceNoteId))
        }
        val recordResultViewModel: RecordResultViewModel = hiltViewModel(parentEntry)
        val uiState = recordResultViewModel.uiState.collectAsState()
        val result = (uiState.value as? com.roro.recorder.presentation.viewModel.RecordResultUiState.Success)?.result

        // result null이면 검색 화면 진입 자체 막기
        if (result == null) {
            navController.popBackStack()
            return@composable
        }

        SearchResultScreen(
            navController = navController,
            result = result,
            onSeek = { recordResultViewModel.seekTo(it) }
        )
    }
}

