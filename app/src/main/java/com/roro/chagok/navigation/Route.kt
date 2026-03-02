package com.roro.chagok.navigation

/**
 * 기능 설명: 네비게이션 화면 세팅
 *
 * object 보여줄 화면 : NavigationRoute("key값")
 *
 * 만약 화면안에 화면을 넣고 싶으면
 * object 부모화면 : NavigationRoute("key값"){
 *      object 자식 화면 : NavigationRoute("부보key값/자식key값")
 * }
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
sealed class NavigationRoute(
    val route: String
){
    object StorageScreen : NavigationRoute("storage")
    object RecorderScreen: NavigationRoute("recorder")
    object ParentScreen : NavigationRoute("parentScreen") {
        object ChildScreenA: NavigationRoute("${ParentScreen.route}/childA")
        object ChildScreenB: NavigationRoute("parentScreen/childB")
        object ChildScreenC: NavigationRoute("parentScreen/childC")
    }
}