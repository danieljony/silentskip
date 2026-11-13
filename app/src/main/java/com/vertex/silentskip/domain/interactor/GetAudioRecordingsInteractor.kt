package com.vertex.silentskip.domain.interactor

import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.domain.contract.NoInputInteractor
import com.vertex.silentskip.domain.model.AudioRecordModel

interface GetAudioRecordingsInteractor : NoInputInteractor<DataWrapper<List<AudioRecordModel>>>
