package com.roro.core.gemma

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.SocketException
import java.net.URL
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GemmaDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val MODEL_FILENAME = "gemma-4-E2B-it.litertlm"
        private const val DOWNLOAD_URL =
            "https://huggingface.co/huggingworld/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm"
    }

    val modelFile: File
        get() = File(context.filesDir, MODEL_FILENAME)

    fun isModelDownloaded(): Boolean = modelFile.exists()

    private val wakeLock by lazy {
        (context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager)
            .newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "chagok:DownloadWakeLock")
    }

    fun download(): Flow<GemmaDownloadState> = flow {
        if (isModelDownloaded()) {
            emit(GemmaDownloadState.Completed)
            return@flow
        }

        wakeLock.acquire(60 * 60 * 1000L)
        emit(GemmaDownloadState.Downloading(0f))
        val tempFile = File(context.filesDir, "$MODEL_FILENAME.tmp")

        try {
            val connection = URL(DOWNLOAD_URL).openConnection() as HttpURLConnection
            connection.apply {
                requestMethod = "GET"
                connectTimeout = 15_000
                readTimeout = 60_000
                connect()
            }

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("서버 오류: ${connection.responseCode}")
            }

            val totalBytes = connection.contentLengthLong
            var downloadedBytes = 0L

            connection.inputStream.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        if (totalBytes > 0) {
                            emit(
                                GemmaDownloadState.Downloading(
                                    downloadedBytes.toFloat() / totalBytes.toFloat()
                                )
                            )
                        }
                    }
                }
            }

            tempFile.renameTo(modelFile)
            emit(GemmaDownloadState.Completed)
            Timber.d("✅ Gemma 다운로드 완료")

        } catch (e: Exception) {
            Timber.tag("GemmaDownload").e(e, "❌ 예외 발생: ${e::class.simpleName} / ${e.message}")
            tempFile.delete()
            val error = when (e) {
                is UnknownHostException,
                is SocketException -> GemmaDownloadState.Error.NetworkLost
                else -> GemmaDownloadState.Error.Unknown(e.message ?: "알 수 없는 오류")
            }
            Timber.e(e, "❌ Gemma 다운로드 실패: $error")
            emit(error)
        }finally {
            if (wakeLock.isHeld) wakeLock.release()
        }
    }.flowOn(Dispatchers.IO)

    fun deleteModel(): Boolean {
        return try {
            if (modelFile.exists()) {
                modelFile.delete().also {
                    Timber.d("✅ Gemma 모델 삭제 완료")
                }
            } else {
                Timber.d("모델 파일 없음")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ 모델 삭제 실패")
            false
        }
    }
}