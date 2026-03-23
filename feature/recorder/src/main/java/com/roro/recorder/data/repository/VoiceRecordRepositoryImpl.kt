package com.roro.recorder.data.repository

import android.Manifest
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import com.roro.core.dao.VoiceRecordDao
import com.roro.core.mapper.toDomain
import com.roro.core.mapper.toEntity
import com.roro.core.model.VoiceRecord
import com.roro.recorder.domain.repository.VoiceRecordRepository
import java.io.File
import java.io.FileOutputStream
import java.util.UUID


class VoiceRecordRepositoryImpl(
    private val dao: VoiceRecordDao
) : VoiceRecordRepository {
    override fun startRecording() {
        TODO("Not yet implemented")
    }

    override fun stopRecording(): File {
        TODO("Not yet implemented")
    }

    override fun isRecording(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun saveVoiceRecord(record: VoiceRecord) {
        dao.upsert(record.toEntity())
    }

    override suspend fun getVoiceRecord(voiceNoteId: UUID): VoiceRecord? {
        return dao.getByVoiceNoteId(voiceNoteId)?.toDomain()
    }
}