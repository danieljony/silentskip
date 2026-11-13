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

class SaveAppSettingsInteractorTest {

    private val repository = mockk<AppSettingsRepository>()
    private lateinit var sut: SaveAppSettingsInteractorImpl

    @BeforeEach
    fun setUp() {
        sut = SaveAppSettingsInteractorImpl(repository)
    }

    @Test
    fun `when execute with settings expect save in repository`() = runTest {
        val settings = AppSettings(isSoundDetectionEnabled = true, isPremiumSubscriber = true)
        coEvery { repository.saveAppSettings(settings) } returns DataWrapper.Success(Unit)

        val result = sut.execute(settings)

        assertEquals(DataWrapper.Success(Unit), result)
        coVerify { repository.saveAppSettings(settings) }
        confirmVerified(repository)
    }
}
