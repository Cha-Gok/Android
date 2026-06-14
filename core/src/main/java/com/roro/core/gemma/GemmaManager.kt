package com.roro.core.gemma

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.ExperimentalApi
import com.google.ai.edge.litertlm.ExperimentalFlags
import com.google.ai.edge.litertlm.SamplerConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GemmaManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gemmaDownloadManager: GemmaDownloadManager
) {
    private var engine: Engine? = null

    val isInitialized: Boolean
        get() = engine != null

    fun initialize() {
        Timber.tag("GemmaManager").d("🚀 initialize() 호출됨")
        if (engine != null) return

        CoroutineScope(Dispatchers.IO).launch {
            tryInitialize()
        }
    }

    private suspend fun tryInitialize() {
        try {
            if (!gemmaDownloadManager.isModelDownloaded()) {
                Timber.tag("GemmaManager").e("❌ 모델 파일 없음, 초기화 중단")
                return
            }

            @OptIn(ExperimentalApi::class)
            ExperimentalFlags.enableSpeculativeDecoding = true

            val config = EngineConfig(
                modelPath = gemmaDownloadManager.modelFile.absolutePath,
                backend = Backend.GPU(),
                audioBackend = Backend.CPU(),
                maxNumTokens = 4096,
                cacheDir = context.cacheDir.absolutePath
            )
            engine = Engine(config)
            engine!!.initialize()
            Timber.tag("GemmaManager").d("✅ Gemma 초기화 완료")
        } catch (e: Exception) {
            Timber.tag("GemmaManager").e(e, "❌ Gemma 초기화 실패")
        }
    }

    suspend fun generate(prompt: String): String {
        return withContext(Dispatchers.IO) {
            try {
                engine?.createConversation()?.use { conversation ->
                    val response = conversation.sendMessage(prompt)
                    Timber.d("response: $response")
                    Timber.d("fields: ${response.javaClass.declaredFields.map { it.name }}")
                    response.toString()
                } ?: ""
            } catch (e: Exception) {
                Timber.tag("GemmaManager").e(e, "❌ Gemma 생성 실패")
                ""
            }
        }
    }

    suspend fun generateWithAudio(audioPath: String, textPrompt: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val conversationConfig = ConversationConfig(
                    samplerConfig = SamplerConfig(temperature = 0.0, topK = 1, topP = 0.0)
                )

                engine?.createConversation(conversationConfig)?.use { conversation ->
                    val response = conversation.sendMessage(
                        Contents.of(
                            Content.AudioFile(audioPath),
                            Content.Text(textPrompt),
                        )
                    )
                    response.toString()
                } ?: ""
            } catch (e: Exception) {
                Timber.tag("GemmaManager").e(e, "❌ 오디오 생성 실패")
                ""
            }
        }
    }

    fun close() {
        engine?.close()
        engine = null
        Timber.tag("GemmaManager").d("✅ Gemma 엔진 종료")
    }
}