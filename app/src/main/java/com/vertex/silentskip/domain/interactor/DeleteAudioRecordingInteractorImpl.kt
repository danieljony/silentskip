package com.vertex.silentskip.domain.interactor

import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.domain.repository.AudioRecordingRepository
import javax.inject.Inject

class DeleteAudioRecordingInteractorImpl @Inject constructor(
    private val repository: AudioRecordingRepository
) : DeleteAudioRecordingInteractor {
    override suspend fun execute(input: String): DataWrapper<Unit> {
        return repository.deleteRecording(input)
    }
}
