package com.roro.chagok

import android.app.Application
import com.roro.core.log.DebugTreeWithLine
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * 기능 설명:
 * - 앱이 시작될 때 가장 먼저 생성되는 Application
 * - Hilt 의존성 주입 시작 지점
 *
 * 초기 설정:
 * 1. HiltAndroidApp을 통해 앱 전역 DI 컨테이너 생성
 * 2. Timber 로그 라이브러리 초기화
 * 3. 추후 생기면 입력...
 *
 * 사용 목적:
 * - 전역 라이브러리 초기화
 * - DI 시작 지점®
 * - 앱 전체 설정 관리
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTreeWithLine())
        }
    }
}