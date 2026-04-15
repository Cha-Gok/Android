package com.roro.recorder.domain.usecase

import javax.inject.Inject

/**
 * 기능 설명:
 * - 텍스트에서 키워드를 추출하는 UseCase
 * - 불용어 제거 후 빈도수 기반으로 상위 N개 키워드 반환 (우선 10개)
 * - DB 저장은 하지 않음 (SaveRecordingUseCase에서 일괄 저장)
 *
 * @author hyeonseo
 * @since 2026. 04. 12.
 */
class ExtractKeywordsUseCase @Inject constructor() {
    operator fun invoke(text: String, topN: Int = 10): List<String> {
        if (text.isBlank()) return emptyList()

        val stopWords = setOf(
            "이", "가", "은", "는", "을", "를", "의", "에", "도", "로", "와", "과",
            "그리고", "그런데", "하지만", "그래서", "때문에", "있습니다", "합니다", "입니다",
            "했습니다", "됩니다", "것입니다", "있는", "하는", "되는"
        )

        return text.split(Regex("\\s+"))
            .map { it.trim() }
            .filter { it.length >= 2 && it !in stopWords }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(topN)
            .map { it.key }
    }
}