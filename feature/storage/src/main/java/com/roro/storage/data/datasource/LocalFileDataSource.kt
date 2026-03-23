package com.roro.storage.data.datasource

import android.content.Context
import com.roro.core.model.Folder
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.UUID
import java.io.File
import javax.inject.Inject

/**
 * 로컬 파일 시스템 접근을 담당하는 DataSource
 *
 * 앱 전용 저장소(Android/data/<package>/files) 아래 구조를 관리한다.
 *
 * base: ChaGok/
 * - 전체 노트/           (앱 시작 시 자동 생성, 삭제 불가)
 *   - <user folders...>  (사용자가 만든 폴더)
 *     - <note item...>   (녹음 저장 시 생성되는 하위 폴더)
 *       - audio/         (녹음 파일)
 *       - transcript/    (전사문)
 * - 휴지통/              (고정)
 */
class LocalFileDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun createVoiceNote(folderName: String?): File {

        // 외부 앱 전용 저장소 (폰 파일 관리자에서도 확인 가능)
        val baseDir = context.getExternalFilesDir(null)
            ?: throw IllegalStateException("외부 저장소 경로를 가져올 수 없습니다.")

        // 혹시 모를 상황 대비 디렉토리 확인
        if (!baseDir.exists()) {
            baseDir.mkdirs()
        }

        val now = System.currentTimeMillis()

        // folderName이 없으면 UUID 사용
        val rawName = folderName ?: UUID.randomUUID().toString()

        // 파일명에 사용할 수 없는 문자 제거
        val safeName = rawName.replace("/", "_")

        val audioFile = File(baseDir, "${safeName}_record_${now}.m4a")

        return try {

            val created = if (!audioFile.exists()) {
                audioFile.createNewFile()
            } else {
                false
            }

            Timber.d("파일 생성 시도")
            Timber.d("baseDir = ${baseDir.absolutePath}")
            Timber.d("filePath = ${audioFile.absolutePath}")
            Timber.d("created = $created, exists = ${audioFile.exists()}")

            audioFile

        } catch (e: Exception) {
            Timber.e(e, "VoiceNote 파일 생성 실패")
            audioFile
        }
    }

}