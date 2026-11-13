package com.vertex.silentskip.domain.repository

import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    fun getAppSettingsFlow(): Flow<AppSettings>
    suspend fun getAppSettings(): DataWrapper<AppSettings>
    suspend fun saveAppSettings(settings: AppSettings): DataWrapper<Unit>
    suspend fun toggleSubscription(isSubscribed: Boolean): DataWrapper<AppSettings>
}
