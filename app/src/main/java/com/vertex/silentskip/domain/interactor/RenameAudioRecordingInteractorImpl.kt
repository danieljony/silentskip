package com.vertex.silentskip.domain.interactor

import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.domain.model.RenameAudioInput
import com.vertex.silentskip.domain.repository.AudioRecordingRepository
import javax.inject.Inject

class RenameAudioRecordingInteractorImpl @Inject constructor(
    private val repository: AudioRecordingRepository
) : RenameAudioRecordingInteractor {
    override suspend fun execute(input: RenameAudioInput): DataWrapper<Unit> {
        return repository.renameRecording(input.id, input.newName)
    }
}
