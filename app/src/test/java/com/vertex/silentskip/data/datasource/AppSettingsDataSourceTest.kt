package com.vertex.silentskip.data.datasource

import android.content.Context
import android.content.SharedPreferences
import com.vertex.silentskip.domain.model.AppSettings
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AppSettingsDataSourceTest {

    private val context = mockk<Context>(relaxed = true)
    private val sharedPreferences = mockk<SharedPreferences>(relaxed = true)
    private val editor = mockk<SharedPreferences.Editor>(relaxed = true)
    private lateinit var sut: AppSettingsDataSource

    @BeforeEach
    fun setUp() {
        every { context.getSharedPreferences(any(), any()) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.putBoolean(any(), any()) } returns editor
        every { editor.apply() } returns Unit

        every { sharedPreferences.getBoolean("key_sound_detection", true) } returns true
        every { sharedPreferences.getBoolean("key_premium_subscriber", false) } returns false

        sut = AppSettingsDataSource(context)
    }

    @Test
    fun `when default settings expect sound detection enabled true`() {
        val settings = sut.getSettings()
        assertEquals(true, settings.isSoundDetectionEnabled)
        assertEquals(false, settings.isPremiumSubscriber)
    }

    @Test
    fun `when saveSettings expect updated settings saved`() {
        val newSettings = AppSettings(isSoundDetectionEnabled = false, isPremiumSubscriber = true)
        sut.saveSettings(newSettings)
        val loaded = sut.getSettings()
        assertEquals(false, loaded.isSoundDetectionEnabled)
        assertEquals(true, loaded.isPremiumSubscriber)
    }

    @Test
    fun `when toggleSubscription expect updated subscription`() {
        val updated = sut.toggleSubscription(true)
        assertEquals(true, updated.isPremiumSubscriber)
    }
}
