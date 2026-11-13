package com.vertex.silentskip.data.repository

import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.common.safeCall
import com.vertex.silentskip.data.datasource.AudioStorageDataSource
import com.vertex.silentskip.domain.model.AudioRecordModel
import com.vertex.silentskip.domain.repository.AudioRecordingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AudioRecordingRepositoryImpl @Inject constructor(
    private val dataSource: AudioStorageDataSource
) : AudioRecordingRepository {

    override fun getRecordingsFlow(): Flow<List<AudioRecordModel>> {
        return dataSource.recordingsFlow
    }

    override suspend fun getRecordings(): DataWrapper<List<AudioRecordModel>> {
        return safeCall { dataSource.getRecordings() }
    }

    override suspend fun renameRecording(id: String, newName: String): DataWrapper<Unit> {
        return safeCall {
            val success = dataSource.renameRecording(id, newName)
            if (!success) {
                throw IllegalStateException("Failed to rename file")
            }
        }
    }

    override suspend fun deleteRecording(id: String): DataWrapper<Unit> {
        return safeCall {
            val success = dataSource.deleteRecording(id)
            if (!success) {
                throw IllegalStateException("Failed to delete file")
            }
        }
    }

    override suspend fun saveRecording(model: AudioRecordModel): DataWrapper<Unit> {
        return safeCall {
            dataSource.refreshRecordings()
        }
    }
}
