package com.vertex.silentskip.domain.interactor

import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.domain.model.AppSettings
import com.vertex.silentskip.domain.repository.AppSettingsRepository
import javax.inject.Inject

class GetAppSettingsInteractorImpl @Inject constructor(
    private val repository: AppSettingsRepository
) : GetAppSettingsInteractor {
    override suspend fun execute(): DataWrapper<AppSettings> {
        return repository.getAppSettings()
    }
}
