package com.roro.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * 기능 설명: String 기반 확장 함수
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */

/**
 * Long타입의 시간을 포맷 해주는 확장함수
 * "yy.MM.dd HH:mm"형식을 원하는 파라미터로 넣어주면 된다.
 *
 * @param
 * @return
 *
 * @author sehoon
 * @since 2026. 3. 24.
 * @modified
 */
fun Long.formatDate(pattern: String = "yyyy.MM.dd"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

/**
 * Long 타입의 시간을 "오전/오후 h:mm" 형식으로 변환하는 확장 함수
 * 예: 11:11 PM -> 오후 11:11
 *
 * @return 포맷된 시간 문자열
 * @author sehoon
 * @since 2026. 4. 19.
 */
fun Long.formatTime(): String {
    val sdf = SimpleDateFormat("a h:mm", Locale.KOREAN)
    return sdf.format(Date(this))
}

fun Long.toDeletedAtString(): String {
    val now = System.currentTimeMillis()
    val diffMillis = now - this
    val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

    val sdfYear = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())

    return when {
        // 오늘 삭제 (24시간 미만 혹은 날짜 기준 오늘)
        diffDays < 1 -> "오늘 삭제됨"

        // 7일 이내
        diffDays <= 7 -> "${diffDays}일 전 삭제됨"

        // 1달(30일) 이내 -> 날짜 형식
        diffDays <= 30 -> "${sdfYear.format(Date(this))} 삭제됨"

        // 1달 초과 (31일 이상) -> N개월 전
        else -> {
            val months = diffDays / 30
            "${months}개월 전 삭제됨"
        }
    }
}

/**
 * Double 타입의 초(seconds) 데이터를 "n시간 n분 n초" 형식으로 변환하는 확장 함수
 * 예: 3661.0 -> "1시간 1분 1초", 65.0 -> "1분 5초"
 *
 * @return 포맷된 시간 문자열
 * @author sehoon
 * @since 2026. 05. 15.
 */
fun Double.toTimeFormat(): String {
    val totalSeconds = this.toLong()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val result = StringBuilder()

    if (hours > 0) {
        result.append("${hours}시간 ")
    }
    if (minutes > 0) {
        result.append("${minutes}분 ")
    }
    // 초는 앞의 단위가 있어도 0초가 아니면 표시, 혹은 전체가 0일 때 "0초" 표시
    if (seconds > 0 || (hours == 0L && minutes == 0L)) {
        result.append("${seconds}초")
    }

    return result.toString().trim()
}

/**
 * String 형식 uuid -> UUID 형식으로 변환
 *
 * @param
 * @return
 *
 * @author sehoon
 * @since 2026. 5. 24.
 * @modified
 */
fun String.toUUIDOrNull():UUID? = runCatching { UUID.fromString(this) }.getOrNull()
