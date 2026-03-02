package com.roro.core.util

import android.view.View

/**
 * 기능 설명: UI 기반 확장 함수
 *
 * @author sehoon
 * @since 2026. 2. 28.
 */

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}