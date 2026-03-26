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