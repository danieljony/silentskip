package com.vertex.silentskip.domain.interactor

import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.domain.contract.NoInputInteractor
import com.vertex.silentskip.domain.model.AppSettings

interface GetAppSettingsInteractor : NoInputInteractor<DataWrapper<AppSettings>>
