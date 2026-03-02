package com.roro.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 기능 설명: String 기반 확장 함수
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */

fun Long.toDate(): String {
    val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
    return sdf.format(Date(this))
}