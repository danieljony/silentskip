package com.vertex.silentskip.domain.interactor

import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.domain.contract.Interactor

interface DeleteAudioRecordingInteractor : Interactor<String, DataWrapper<Unit>>
