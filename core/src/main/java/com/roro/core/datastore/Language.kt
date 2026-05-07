package com.roro.core.datastore

import java.util.Locale

enum class Language(val code: String, val locale: Locale) {
    KOREAN("ko", Locale("ko", "KR")),
    ENGLISH("en", Locale("en", "US"));

    companion object {
        fun from(code: String): Language {
            return entries.find { it.code == code } ?: KOREAN
        }
    }
}