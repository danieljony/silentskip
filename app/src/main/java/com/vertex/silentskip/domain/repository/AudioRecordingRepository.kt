package com.vertex.silentskip.domain.repository

import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.domain.model.AudioRecordModel
import kotlinx.coroutines.flow.Flow

interface AudioRecordingRepository {
    fun getRecordingsFlow(): Flow<List<AudioRecordModel>>
    suspend fun getRecordings(): DataWrapper<List<AudioRecordModel>>
    suspend fun renameRecording(id: String, newName: String): DataWrapper<Unit>
    suspend fun deleteRecording(id: String): DataWrapper<Unit>
    suspend fun saveRecording(model: AudioRecordModel): DataWrapper<Unit>
}
