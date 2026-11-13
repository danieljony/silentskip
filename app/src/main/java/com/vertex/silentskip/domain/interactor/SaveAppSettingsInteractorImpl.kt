package com.vertex.silentskip.domain.interactor

import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.domain.model.AppSettings
import com.vertex.silentskip.domain.repository.AppSettingsRepository
import javax.inject.Inject

class SaveAppSettingsInteractorImpl @Inject constructor(
    private val repository: AppSettingsRepository
) : SaveAppSettingsInteractor {
    override suspend fun execute(input: AppSettings): DataWrapper<Unit> {
        return repository.saveAppSettings(input)
    }
}
