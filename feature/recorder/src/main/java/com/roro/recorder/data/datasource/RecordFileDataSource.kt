//package com.roro.record.data.datasource
//
//import android.content.Context
//import dagger.hilt.android.qualifiers.ApplicationContext
//import timber.log.Timber
//import java.io.File
//import java.util.UUID
//import javax.inject.Inject
//
///**
// * [record] 모듈 전용 로컬 파일 데이터 소스
// * * 외부 모듈(storage)에 의존하지 않고 녹음 파일을 생성하기 위해 독립적으로 구성함.
// * 저장 위치: /storage/emulated/0/Android/data/<package>/files/
// */
//class RecordFileDataSource @Inject constructor(
//    @ApplicationContext private val context: Context
//) {
//
//    /**
//     * 녹음을 위한 새로운 .m4a 파일을 생성합니다.
//     * @param folderName 파일명 접두어로 사용될 이름 (선택)
//     * @return 생성된 파일 객체
//     */
//    fun createVoiceNoteFile(folderName: String? = null): File {
//        // 1. 앱 전용 외부 저장소 경로 확보
//        val baseDir = context.getExternalFilesDir(null)
//            ?: throw IllegalStateException("외부 저장소에 접근할 수 없습니다.")
//
//        // 2. 디렉토리가 없으면 생성
//        if (!baseDir.exists()) {
//            baseDir.mkdirs()
//        }
//
//        val now = System.currentTimeMillis()
//
//        // 3. 파일명 세팅 (폴더명이 없으면 'REC' 사용, 특수문자 제거)
//        val prefix = if (!folderName.isNullOrBlank()) {
//            folderName.replace(Regex("[^a-zA-Z0-9가-힣]"), "_")
//        } else {
//            "REC"
//        }
//
//        // 4. 최종 파일 객체 생성 (중복 방지를 위해 UUID 또는 타임스탬프 결합)
//        val fileName = "${prefix}_${now}.m4a"
//        val audioFile = File(baseDir, fileName)
//
//        return try {
//            if (audioFile.createNewFile()) {
//                Timber.d("녹음 파일 생성 성공: ${audioFile.absolutePath}")
//            } else {
//                Timber.w("파일이 이미 존재함: ${audioFile.absolutePath}")
//            }
//            audioFile
//        } catch (e: Exception) {
//            Timber.e(e, "파일 생성 중 예외 발생")
//            audioFile // 예외 발생 시에도 객체는 반환 (이후 단계에서 체크)
//        }
//    }
//
//    /**
//     * 녹음 실패 혹은 취소 시 생성된 파일을 삭제합니다.
//     */
//    fun deleteFile(file: File): Boolean {
//        return if (file.exists()) {
//            val deleted = file.delete()
//            Timber.d("파일 삭제 여부 ($deleted): ${file.path}")
//            deleted
//        } else false
//    }
//}