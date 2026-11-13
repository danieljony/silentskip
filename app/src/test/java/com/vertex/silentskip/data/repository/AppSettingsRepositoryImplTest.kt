package com.vertex.silentskip.data.repository

import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.data.datasource.AppSettingsDataSource
import com.vertex.silentskip.domain.model.AppSettings
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AppSettingsRepositoryImplTest {

    private val dataSource = mockk<AppSettingsDataSource>()
    private lateinit var sut: AppSettingsRepositoryImpl

    @BeforeEach
    fun setUp() {
        sut = AppSettingsRepositoryImpl(dataSource)
    }

    @Test
    fun `when getAppSettings expect settings from datasource`() = runTest {
        val settings = AppSettings(isSoundDetectionEnabled = false, isPremiumSubscriber = true)
        every { dataSource.getSettings() } returns settings

        val result = sut.getAppSettings()

        assertEquals(DataWrapper.Success(settings), result)
        verify { dataSource.getSettings() }
    }

    @Test
    fun `when getAppSettingsFlow expect flow from datasource`() {
        val flow = MutableStateFlow(AppSettings(true, true))
        every { dataSource.settingsFlow } returns flow

        val result = sut.getAppSettingsFlow()

        assertEquals(flow, result)
        verify { dataSource.settingsFlow }
    }

    @Test
    fun `when saveAppSettings expect save in datasource`() = runTest {
        val settings = AppSettings(true, true)
        every { dataSource.saveSettings(settings) } just runs

        val result = sut.saveAppSettings(settings)

        assertEquals(DataWrapper.Success(Unit), result)
        verify { dataSource.saveSettings(settings) }
    }

    @Test
    fun `when toggleSubscription expect toggle in datasource`() = runTest {
        val settings = AppSettings(true, true)
        every { dataSource.toggleSubscription(true) } returns settings

        val result = sut.toggleSubscription(true)

        assertEquals(DataWrapper.Success(settings), result)
        verify { dataSource.toggleSubscription(true) }
    }
}
