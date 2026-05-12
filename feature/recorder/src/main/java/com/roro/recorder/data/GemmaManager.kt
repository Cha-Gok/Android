package com.roro.recorder.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
                val config = EngineConfig(
                    modelPath = SDCARD_MODEL_PATH,
                    backend = Backend.CPU(),
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
    }

    suspend fun generate(prompt: String): String {
        return withContext(Dispatchers.IO) {
            try {
                engine?.createConversation()?.use { conversation ->
                    val response: Message = conversation.sendMessage(prompt)
                    response.toString()
                } ?: ""
            } catch (e: Exception) {
                Timber.tag("GemmaManager").e(e, "❌ Gemma 생성 실패")
                ""
            }
        }
    }

    fun close() {
        engine?.close()
        engine = null
    }
}