package com.roro.recorder.domain.usecase

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


/**
 * 기능 설명:
 * - 텍스트 번역을 담당하는 UseCase
 * - MLKit Translation을 사용하여 영어 → 한국어 번역
 * - MLKit Summarization의 출력이 항상 영어이므로 한국어로 변환하기 위해 사용
 *
 * @author hyeonseo
 * @since 2026. 04. 12.
 */
class TranslateTextUseCase @Inject constructor() {

    /**
     * 영어 텍스트를 한국어로 번역
     * - 번역 모델이 없을 경우 Wi-Fi 환경에서 자동 다운로드
     *
     * @param text 번역할 영어 텍스트
     * @return 한국어로 번역된 텍스트
     *
     * @author hyeonseo
     * @since 2026. 04. 12.
     * @modified
     */
    suspend operator fun invoke(text: String): String {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)  // ← 영어에서
            .setTargetLanguage(TranslateLanguage.KOREAN)   // ← 한국어로
            .build()
        val translator = Translation.getClient(options)
        return try {
            val conditions = DownloadConditions.Builder().requireWifi().build()
            translator.downloadModelIfNeeded(conditions).await()
            translator.translate(text).await()
        } finally {
            translator.close()
        }
    }
}