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

class ToggleSubscriptionInteractorTest {

    private val repository = mockk<AppSettingsRepository>()
    private lateinit var sut: ToggleSubscriptionInteractorImpl

    @BeforeEach
    fun setUp() {
        sut = ToggleSubscriptionInteractorImpl(repository)
    }

    @Test
    fun `when execute with subscription status expect toggle in repository`() = runTest {
        val isSubscribed = true
        val updatedSettings = AppSettings(isSoundDetectionEnabled = true, isPremiumSubscriber = true)
        coEvery { repository.toggleSubscription(isSubscribed) } returns DataWrapper.Success(updatedSettings)

        val result = sut.execute(isSubscribed)

        assertEquals(DataWrapper.Success(updatedSettings), result)
        coVerify { repository.toggleSubscription(isSubscribed) }
        confirmVerified(repository)
    }
}
