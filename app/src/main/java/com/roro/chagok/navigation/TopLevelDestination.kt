package com.roro.chagok.navigation

import com.roro.core.navigation.Routes

data class TopLevelDestination(
    val route: String,
    val label:String
)

val topLevelDestination = listOf(
    TopLevelDestination(route = Routes.STORAGE, label="파일 저장소"),
    TopLevelDestination(route = Routes.RECORDER, label = "녹음")
)

val bottomBarRoutes = topLevelDestination.map { it.route }.toSet()
