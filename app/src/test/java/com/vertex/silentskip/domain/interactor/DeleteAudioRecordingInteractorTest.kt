package com.vertex.silentskip.domain.interactor

import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.domain.repository.AudioRecordingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeleteAudioRecordingInteractorTest {

    private val repository = mockk<AudioRecordingRepository>()
    private lateinit var sut: DeleteAudioRecordingInteractorImpl

    @BeforeEach
    fun setUp() {
        sut = DeleteAudioRecordingInteractorImpl(repository)
    }

    @Test
    fun `when execute with valid id expect delete recording from repository`() = runTest {
        val recordingId = "id1"
        coEvery { repository.deleteRecording(recordingId) } returns DataWrapper.Success(Unit)

        val result = sut.execute(recordingId)

        assertEquals(DataWrapper.Success(Unit), result)
        coVerify { repository.deleteRecording(recordingId) }
        confirmVerified(repository)
    }
}
