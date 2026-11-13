package com.vertex.silentskip.data.repository

import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.common.safeCall
import com.vertex.silentskip.data.datasource.AppSettingsDataSource
import com.vertex.silentskip.domain.model.AppSettings
import com.vertex.silentskip.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class AppSettingsRepositoryImpl @Inject constructor(
    private val dataSource: AppSettingsDataSource
) : AppSettingsRepository {

    override fun getAppSettingsFlow(): StateFlow<AppSettings> {
        return dataSource.settingsFlow
    }

    override suspend fun getAppSettings(): DataWrapper<AppSettings> {
        return safeCall { dataSource.getSettings() }
    }

    override suspend fun saveAppSettings(settings: AppSettings): DataWrapper<Unit> {
        return safeCall { dataSource.saveSettings(settings) }
    }

    override suspend fun toggleSubscription(isSubscribed: Boolean): DataWrapper<AppSettings> {
        return safeCall { dataSource.toggleSubscription(isSubscribed) }
    }
}
