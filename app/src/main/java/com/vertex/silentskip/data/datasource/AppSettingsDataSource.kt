package com.vertex.silentskip.data.datasource

import android.content.Context
import android.content.SharedPreferences
import com.vertex.silentskip.domain.model.AppSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class AppSettingsDataSource @Inject constructor(
    @ApplicationContext context: Context
) {

    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _settingsFlow = MutableStateFlow(loadSettingsFromPrefs())
    val settingsFlow: StateFlow<AppSettings> = _settingsFlow.asStateFlow()

    fun getSettings(): AppSettings {
        return _settingsFlow.value
    }

    fun saveSettings(settings: AppSettings) {
        preferences.edit()
            .putBoolean(KEY_SOUND_DETECTION, settings.isSoundDetectionEnabled)
            .putBoolean(KEY_PREMIUM_SUBSCRIBER, settings.isPremiumSubscriber)
            .apply()

        _settingsFlow.value = settings
    }

    fun toggleSubscription(isSubscribed: Boolean): AppSettings {
        val updated = _settingsFlow.value.copy(isPremiumSubscriber = isSubscribed)
        saveSettings(updated)
        return updated
    }

    private fun loadSettingsFromPrefs(): AppSettings {
        val soundEnabled = preferences.getBoolean(KEY_SOUND_DETECTION, true)
        val isPremium = preferences.getBoolean(KEY_PREMIUM_SUBSCRIBER, false)
        return AppSettings(
            isSoundDetectionEnabled = soundEnabled,
            isPremiumSubscriber = isPremium
        )
    }

    companion object {
        private const val PREFS_NAME = "silentskip_settings_prefs"
        private const val KEY_SOUND_DETECTION = "key_sound_detection"
        private const val KEY_PREMIUM_SUBSCRIBER = "key_premium_subscriber"
    }
}
