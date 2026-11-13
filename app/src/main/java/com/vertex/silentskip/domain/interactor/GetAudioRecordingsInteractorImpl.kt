package com.vertex.silentskip.domain.interactor

import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.domain.model.AudioRecordModel
import com.vertex.silentskip.domain.repository.AudioRecordingRepository
import javax.inject.Inject

class GetAudioRecordingsInteractorImpl @Inject constructor(
    private val repository: AudioRecordingRepository
) : GetAudioRecordingsInteractor {
    override suspend fun execute(): DataWrapper<List<AudioRecordModel>> {
        return repository.getRecordings()
    }
}
