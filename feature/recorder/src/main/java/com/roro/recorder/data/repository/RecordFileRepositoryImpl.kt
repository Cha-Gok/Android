//package com.roro.record.data.repository
//
//import com.roro.core.entity.*
//import com.roro.core.mapper.toEntity
//import com.roro.core.model.Summary
//import com.roro.core.model.Transcript
//import com.roro.core.model.VoiceNote
//import com.roro.core.model.VoiceRecord
//
//import com.roro.record.data.datasource.RecordFileDataSource
//
//import com.roro.recorder.data.datasource.AudioRecordDataSource
//import com.roro.recorder.domain.repository.AudioRecordRepository
//import timber.log.Timber
//import java.io.File
//import java.util.UUID
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class RecordFileRepositoryImpl @Inject constructor(
//    private val fileDataSource: RecordFileDataSource,      // 파일 생성 (내 모듈)
//    private val audioDataSource: AudioRecordDataSource,    // MediaRecorder + GenAI (내 모듈)
//    private val roomDataSource: RecordDataSource       // DB 저장 (내 모듈용 임시)
//) : AudioRecordRepository {
//
//    private var currentFile: File? = null
//    private var _isRecording: Boolean = false
//
//    // 1. 인터페이스의 startRecording 구현
//    override fun startRecording(folderName: String?) {
//        try {
//            // 새 .m4a 파일 생성
//            val file = fileDataSource.createVoiceNoteFile(folderName)
//            currentFile = file
//
//            // MediaRecorder 시작
//            audioDataSource.startRecording(file)
//            _isRecording = true
//
//            Timber.d("녹음 시작 성공: ${file.absolutePath}")
//        } catch (e: Exception) {
//            Timber.e(e, "녹음 시작 중 오류 발생")
//            _isRecording = false
//        }
//    }
//
//    // 2. 인터페이스의 stopAndSaveRecording 구현 (핵심 로직)
//    override suspend fun stopAndSaveRecording(folderId: UUID?) {
//        val file = currentFile ?: return
//
//        try {
//            // 녹음 중지
//            audioDataSource.stopRecording()
//            _isRecording = false
//
//            // ML Kit GenAI를 이용한 진짜 텍스트 추출
//            val scriptContent = audioDataSource.getTranscriptWithGenAI(file)
//
//            // DB 데이터 조립
//            val noteId = UUID.randomUUID()
//            val now = System.currentTimeMillis()
//
//            val voiceNote = VoiceNote(
//                id = noteId,
//                title = "새 녹음_${now}",
//                createdAt = now,
//                updatedAt = now,
//                deletedAt = null,      // 삭제되지 않은 상태이므로 null
//                folderId = folderId    // 인자로 받은 folderId (null 가능)
//            )
//
//            val voiceRecord = VoiceRecord(
//                id = UUID.randomUUID(),
//                audioFilePath = file.absolutePath, // 실제 저장된 파일 경로
//                duration = 0.0,                    // 일단 0.0으로 초기화
//                createdAt = now,
//                voiceNoteId = noteId
//            )
//
//            val transcript = Transcript(
//                id = UUID.randomUUID(),
//                text = scriptContent,              // GenAI가 추출한 실제 텍스트
//                createdAt = now,
//                voiceNoteId = noteId
//            )
//
//            val summary = Summary(
//                id = UUID.randomUUID(),
//                text = "",                         // 초기에는 빈 값, 필요 시 AI 요약 결과 대입
//                createdAt = now,
//                voiceNoteId = noteId
//            )
//
//            // 내 모듈 전용 DB DataSource에 저장
//            roomDataSource.insertFullVoiceNote(
//                voiceNote = voiceNote.toEntity(),      // Mapper를 통해 Entity로 변환
//                voiceRecord = voiceRecord.toEntity(),
//                transcript = transcript.toEntity(),
//                summary = summary.toEntity(),
//                keywords = emptyList()                 // 키워드는 일단 빈 리스트로 시작
//            )
//
//            Timber.d("녹음 및 DB 저장 완료: ${file.name}")
//
//        } catch (e: Exception) {
//            Timber.e(e, "중지 및 저장 중 오류 발생")
//        } finally {
//            currentFile = null
//        }
//    }
//
//    // 3. 인터페이스의 isRecording 구현
//    override fun isRecording(): Boolean = _isRecording
//}