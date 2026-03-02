package com.roro.core.log

import timber.log.Timber

/**
 * 기능 설명: 
 * Timber가 로그를 찍을 때 사용할 tag를 생성하는 함수
 *
 * Timber 호출 시 파일이름:라인 형식으로 나올 수 있게
 * 확장했음
 * 
 * @author sehoon
 * @since 2026. 3. 2.
 */
class DebugTreeWithLine : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String {
        val file = element.fileName ?: "UnknownFile"
        val line = element.lineNumber
        return "$file:$line"
    }
}