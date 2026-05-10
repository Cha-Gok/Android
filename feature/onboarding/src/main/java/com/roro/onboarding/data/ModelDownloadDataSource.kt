package com.roro.onboarding.data

import android.content.Context
import com.google.mlkit.genai.common.DownloadCallback
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.speechrecognition.SpeechRecognition
import com.google.mlkit.genai.speechrecognition.SpeechRecognizerOptions
import com.google.mlkit.genai.speechrecognition.speechRecognizerOptions
import com.google.mlkit.genai.summarization.Summarization
import com.google.mlkit.genai.summarization.SummarizerOptions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

class ModelDownloadDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun downloadSTT() {
        val options = speechRecognizerOptions {
            locale = Locale("ko", "KR")
            preferredMode = SpeechRecognizerOptions.Mode.MODE_BASIC
        }
        val speechRecognizer = SpeechRecognition.getClient(options)
        try {
            when (speechRecognizer.checkStatus()) {
                FeatureStatus.AVAILABLE -> Timber.Forest.d("STT 사용 가능")
                FeatureStatus.DOWNLOADABLE -> {
                    speechRecognizer.download().collect { }
                    Timber.Forest.d("STT 다운로드 완료")
                }
                FeatureStatus.UNAVAILABLE -> throw Exception("STT 미지원 기기")
                FeatureStatus.DOWNLOADING -> Timber.Forest.d("STT 다운로드 중")
            }
        } finally {
            speechRecognizer.close()
        }
    }

    suspend fun downloadSummarize() {
        val options = SummarizerOptions.builder(context)
            .setInputType(SummarizerOptions.InputType.ARTICLE)
            .setOutputType(SummarizerOptions.OutputType.ONE_BULLET)
            .setLanguage(SummarizerOptions.Language.ENGLISH)
            .build()
        val summarizer = Summarization.getClient(options)
        try {
            when (summarizer.checkFeatureStatus().await()) {
                3 -> Timber.Forest.d("요약 사용 가능")
                1 -> {
                    summarizer.downloadFeature(object : DownloadCallback {
                        override fun onDownloadStarted(b: Long) {}
                        override fun onDownloadProgress(b: Long) {}
                        override fun onDownloadCompleted() {}
                        override fun onDownloadFailed(e: GenAiException) {
                            throw e
                        }
                    }).await()
                    Timber.Forest.d("요약 다운로드 완료")
                }
                0 -> throw Exception("요약 미지원 기기")
                2 -> Timber.Forest.d("요약 다운로드 중")
            }
        } finally {
            summarizer.close()
        }
    }

    suspend fun downloadTranslate() {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.KOREAN)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()
        val translator = Translation.getClient(options)
        try {
            translator.downloadModelIfNeeded().await()
            Timber.Forest.d("번역 다운로드 완료")
        } finally {
            translator.close()
        }
    }
}