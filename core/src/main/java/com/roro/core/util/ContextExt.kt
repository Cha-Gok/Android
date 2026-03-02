package com.roro.core.util

import android.content.Context
import android.widget.Toast

/**
 * 기능 설명: Context가 포함 되는 확장 함수
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */
fun Context.toast(message: String) {
    Toast.makeText(
        this,
        message,
        Toast.LENGTH_SHORT
    ).show()
}
