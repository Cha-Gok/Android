package com.roro.chagok.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.roro.core.ui.theme.ChaGokTheme

/**
 * 기능 설명:
 * - 앱의 주요 화면으로 이동할 수 있는 바텀 네비게이션 바
 * - topLevelDestination 목록을 기반으로 탭을 구성
 *
 * @param navController 네비게이션 컨트롤러
 * @param currentRoute 현재 화면의 route
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
@Composable
fun BottomBar(
    navController: NavController,
    currentRoute:String?
){
    NavigationBar {
        topLevelDestination.forEach { destination ->
            val selected = (currentRoute == destination.route)

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        // 탭 이동 옵션
                        launchSingleTop = true
                        restoreState = true

                        // startDestination까지 pop해서 back stack정리
                        popUpTo(navController.graph.startDestinationId){
                            saveState = true
                        }
                    }
                },
                label = {Text(destination.label)},
                icon = { /* 아이콘 추가 */}
            )
        }
    }
}

@Preview
@Composable
fun PreviewBottomBar() {
    ChaGokTheme {
        BottomBar(
            navController = rememberNavController(),
            currentRoute = "storage"
        )
    }
}