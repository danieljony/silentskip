package com.vertex.silentskip.domain.interactor

import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.domain.contract.Interactor
import com.vertex.silentskip.domain.model.AppSettings

interface SaveAppSettingsInteractor : Interactor<AppSettings, DataWrapper<Unit>>
