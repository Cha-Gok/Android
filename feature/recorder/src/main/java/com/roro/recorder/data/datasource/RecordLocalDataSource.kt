//package com.roro.recorder.data.datasource
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.content.Context
//import android.media.AudioFormat
//import android.media.AudioRecord
//import android.media.MediaRecorder
//import androidx.annotation.RequiresPermission
//import dagger.hilt.android.qualifiers.ApplicationContext
//import timber.log.Timber
//import java.util.UUID
//import java.io.File
//import java.io.FileOutputStream
//import javax.inject.Inject
//
//class RecordLocalDataSource @Inject constructor(
//    @ApplicationContext private val context: Context
//) {
//    private var audioRecord: AudioRecord? = null
//    private var isRecording = false
//    private var recordingThread: Thread? = null
//
//
//    private var recorder: MediaRecorder? = null
//    private var outputFile: File? = null
//
//    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
//    fun startRecording(fileName: String): File {
//        // 외부 앱 전용 저장소 (폰 파일 관리자에서도 확인 가능)
//        val baseDir = context.getExternalFilesDir(null)
//            ?: throw IllegalStateException("외부 저장소 경로를 가져올 수 없습니다.")
//
//        // 파일명에 사용할 수 없는 문자 제거
//        val safeName = fileName.replace("/", "_")
//
//        val now = System.currentTimeMillis()
//
//        outputFile = File(baseDir,  "${safeName}_record_${now}.m4a")
//
//        recorder = MediaRecorder().apply {
//            setAudioSource(MediaRecorder.AudioSource.MIC)
//            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
//            setOutputFile(outputFile!!.absolutePath)
//            prepare()
//            start()
//        }
//
//        Timber.d("녹음 시작 - ${outputFile!!.absolutePath}")
//        return outputFile!!
//    }
//
//    fun pauseRecording() {
//        if (isRecording) {
//            audioRecord?.stop()
//            isRecording = false
//            Timber.d("녹음 일시정지")
//        }
//    }
//
//    fun resumeRecording() {
//        if (!isRecording) {
//            audioRecord?.startRecording()
//            isRecording = true
//            Timber.d("녹음 재개")
//        }
//    }
//
//    fun stopRecording(): File {
//        recorder?.apply {
//            stop()
//            release()
//        }
//        recorder = null
//
//        Timber.d("녹음 종료 - ${outputFile!!.absolutePath}")
//        return outputFile!!
//    }
//
//    fun isRecording(): Boolean = isRecording
//
//    /////
//    suspend fun transcribe(file: File): String {
//        // TODO: ML Kit STT
//        return "변환된 텍스트"
//    }
//}