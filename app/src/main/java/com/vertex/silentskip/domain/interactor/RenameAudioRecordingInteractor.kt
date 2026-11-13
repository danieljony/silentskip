package com.vertex.silentskip.domain.interactor

import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.domain.contract.Interactor
import com.vertex.silentskip.domain.model.RenameAudioInput

interface RenameAudioRecordingInteractor : Interactor<RenameAudioInput, DataWrapper<Unit>>
