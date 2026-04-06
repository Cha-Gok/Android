package com.roro.core.datastore

enum class Language(val code: String) {
    KOREAN("ko"),
    ENGLISH("en");

    companion object {
        fun from(code: String): Language {
            return entries.find { it.code == code } ?: KOREAN
        }
    }
}