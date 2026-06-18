package com.roro.core.domain.model

enum class SummaryStatus(val label: String) {
    NONE("전사 실패"),     // STT 결과 없음
    SUCCESS("요약 성공"),  // 요약 성공
    FAIL("요약 실패")      // 요약 실패
}