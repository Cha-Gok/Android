package com.roro.recorder.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import com.google.ai.edge.litertlm.SamplerConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.ai.edge.litertlm.ExperimentalApi
import com.google.ai.edge.litertlm.ExperimentalFlags


@Singleton
class GemmaManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var engine: Engine? = null

    companion object {
        private const val MODEL_FILENAME = "gemma4-e2b.litertlm"
        private const val SDCARD_MODEL_PATH = "/sdcard/Download/gemma4-e2b.litertlm"
    }

    fun initialize() {
        Timber.tag("GemmaManager").d("🚀 initialize() 호출됨")
        if (engine != null) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // MTP 활성화 (엔진 초기화 전에 호출)
                @OptIn(ExperimentalApi::class)
                ExperimentalFlags.enableSpeculativeDecoding = true

                val config = EngineConfig(
                    modelPath = SDCARD_MODEL_PATH,
                    backend = Backend.GPU(),
                    audioBackend = Backend.CPU(),
                    maxNumTokens = 4096,
                    cacheDir = context.cacheDir.absolutePath
                )
                engine = Engine(config)
                engine!!.initialize()
                Timber.tag("GemmaManager").d("✅ Gemma 초기화 완료 (MTP 활성화)")
            } catch (e: Exception) {
                Timber.tag("GemmaManager").e(e, "❌ Gemma 초기화 실패")
            }
        }
    }

    suspend fun generate(prompt: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val start = System.currentTimeMillis()

                engine?.createConversation()?.use { conversation ->
                    val response: Message = conversation.sendMessage(prompt)
                    val elapsed = System.currentTimeMillis() - start
                    Timber.tag("GemmaManager").d("⏱️ generate 완료: ${elapsed}ms / 프롬프트 길이: ${prompt.length}자")
                    response.toString()
                } ?: ""
            } catch (e: Exception) {
                Timber.tag("GemmaManager").e(e, "❌ Gemma 생성 실패")
                ""
            }
        }
    }

    // GemmaManager에 추가
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
    }
}