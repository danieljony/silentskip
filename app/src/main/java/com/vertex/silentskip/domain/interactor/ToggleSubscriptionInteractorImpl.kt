package com.vertex.silentskip.domain.interactor

import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.domain.model.AppSettings
import com.vertex.silentskip.domain.repository.AppSettingsRepository
import javax.inject.Inject

class ToggleSubscriptionInteractorImpl @Inject constructor(
    private val repository: AppSettingsRepository
) : ToggleSubscriptionInteractor {
    override suspend fun execute(input: Boolean): DataWrapper<AppSettings> {
        return repository.toggleSubscription(input)
    }
}
