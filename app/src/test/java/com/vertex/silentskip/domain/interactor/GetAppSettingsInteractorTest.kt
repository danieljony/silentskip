package com.vertex.silentskip.domain.interactor

import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.domain.model.AppSettings
import com.vertex.silentskip.domain.repository.AppSettingsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetAppSettingsInteractorTest {

    private val repository = mockk<AppSettingsRepository>()
    private lateinit var sut: GetAppSettingsInteractorImpl

    @BeforeEach
    fun setUp() {
        sut = GetAppSettingsInteractorImpl(repository)
    }

    @Test
    fun `when execute expect get app settings from repository`() = runTest {
        val expectedSettings = AppSettings(isSoundDetectionEnabled = true, isPremiumSubscriber = false)
        coEvery { repository.getAppSettings() } returns DataWrapper.Success(expectedSettings)

        val result = sut.execute()

        assertEquals(DataWrapper.Success(expectedSettings), result)
        coVerify { repository.getAppSettings() }
        confirmVerified(repository)
    }
}
