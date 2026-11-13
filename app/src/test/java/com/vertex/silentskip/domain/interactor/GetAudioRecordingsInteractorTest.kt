package com.vertex.silentskip.domain.interactor

import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.domain.model.AudioRecordModel
import com.vertex.silentskip.domain.repository.AudioRecordingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetAudioRecordingsInteractorTest {

    private val repository = mockk<AudioRecordingRepository>()
    private lateinit var sut: GetAudioRecordingsInteractorImpl

    @BeforeEach
    fun setUp() {
        sut = GetAudioRecordingsInteractorImpl(repository)
    }

    @Test
    fun `when execute expect get recordings from repository`() = runTest {
        val expectedList = listOf(
            AudioRecordModel("id1", "rec1", "/path/1", 1000L, 500L, "01/01/2026 10:00", 123L)
        )
        coEvery { repository.getRecordings() } returns DataWrapper.Success(expectedList)

        val result = sut.execute()

        assertEquals(DataWrapper.Success(expectedList), result)
        coVerify { repository.getRecordings() }
        confirmVerified(repository)
    }
}
