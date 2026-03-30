//package com.roro.recorder.data.repository
//
//import com.roro.record.data.datasource.AudioRecordDataSource
//import com.roro.record.data.datasource.RecordFileDataSource
//import com.roro.recorder.data.datasource.AudioRecordDataSource
//import com.roro.recorder.domain.repository.AudioRecordRepository
//import timber.log.Timber
//import java.io.File
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class AudioRecordRepositoryImpl @Inject constructor(
//    private val fileDataSource: RecordFileDataSource,      // 1. 파일 생성용
//    private val audioDataSource: AudioRecordDataSource,    // 2. 녹음 & GenAI용
//    private val recordRoomDataSource: RecordDataSource // 3. DB 저장용 (중요!)
//) : AudioRecordRepository {
//
//    private var currentFile: File? = null
//
//    override fun startRecording() {
//        try {
//            // 1. 내 모듈의 DataSource로 파일 공간 확보
//            val file = recordFileDataSource.createVoiceNote()
//            currentFile = file
//
//            // 2. 녹음 시작
//            audioRecordDataSource.startRecording(file)
//            Timber.d("녹음 시작됨: ${file.absolutePath}")
//        } catch (e: Exception) {
//            Timber.e(e, "녹음 시작 프로세스 오류")
//        }
//    }
//
//    override suspend fun stopAndSave(folderId: UUID?) {
//        val file = currentFile ?: return
//
//        try {
//            // 1. 녹음 중지
//            audioRecordDataSource.stopRecording()
//
//            // 2. ML Kit GenAI로 텍스트 추출 (Advanced Mode)
//            val script = audioRecordDataSource.getTranscriptWithGenAI(file)
//
//            // 3. DB 저장을 위한 엔티티 조립 (동료의 Room 구조에 맞춤)
//            val noteId = UUID.randomUUID()
//            val now = System.currentTimeMillis()
//
//            val voiceNote = VoiceNoteEntity(
//                id = noteId,
//                folderId = folderId,
//                title = "새 녹음 ${now}",
//                createdAt = now,
//                updatedAt = now
//            )
//
//            // 4. DB에 최종 저장
//            roomFileDataSource.createVoiceNote(
//                voiceNote = voiceNote,
//                voiceRecord = VoiceRecordEntity(
//                    id = UUID.randomUUID(),
//                    voiceNoteId = noteId,
//                    filePath = file.absolutePath, // 핵심: 이 경로로 나중에 파일을 찾음
//                    fileSize = file.length()
//                ),
//                transcript = TranscriptEntity(
//                    id = UUID.randomUUID(),
//                    voiceNoteId = noteId,
//                    content = script
//                ),
//                summary = null, // 필요 시 나중에 업데이트
//                keywords = emptyList()
//            )
//
//            Timber.d("저장 완료: ${file.name}")
//        } catch (e: Exception) {
//            Timber.e(e, "저장 프로세스 오류")
//        } finally {
//            currentFile = null
//        }
//    }
//}