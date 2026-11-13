package com.vertex.silentskip.presentation.action

import com.vertex.silentskip.data.datasource.AppSettingsDataSource
import com.vertex.silentskip.domain.model.AppSettings
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AdManagerActionTest {

    private val appSettingsDataSource = mockk<AppSettingsDataSource>()
    private lateinit var sut: AdManagerAction

    @Before
    fun setUp() {
        every { appSettingsDataSource.getSettings() } returns AppSettings(true, false)
        sut = AdManagerAction(appSettingsDataSource)
    }

    @Test
    fun `when non-premium user expect showBannerAd true`() {
        assertTrue(sut.adState.value.showBannerAd)
    }

    @Test
    fun `when onSettingsChanged to premium expect showBannerAd false`() {
        sut.onSettingsChanged(isPremium = true)
        assertFalse(sut.adState.value.showBannerAd)
    }
}
