package com.roro.storage.data.datasource

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import java.util.UUID
import javax.inject.Inject

class RecorderLocalFileDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun createAudioFile(folderName: String? = null): File {

        val baseDir = context.getExternalFilesDir(null)
            ?: throw IllegalStateException("저장소 접근 불가")

        if (!baseDir.exists()) baseDir.mkdirs()

        val now = System.currentTimeMillis()
        val safeName = (folderName ?: UUID.randomUUID().toString())
            .replace("/", "_")

        val file = File(baseDir, "${safeName}_$now.m4a")

        return try {
            if (!file.exists()) file.createNewFile()

            Timber.d("파일 생성: ${file.absolutePath}")
            file

        } catch (e: Exception) {
            Timber.e(e)
            file
        }
    }
}