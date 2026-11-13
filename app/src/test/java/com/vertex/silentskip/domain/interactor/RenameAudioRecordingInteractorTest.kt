package com.vertex.silentskip.domain.interactor

import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.domain.model.RenameAudioInput
import com.vertex.silentskip.domain.repository.AudioRecordingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RenameAudioRecordingInteractorTest {

    private val repository = mockk<AudioRecordingRepository>()
    private lateinit var sut: RenameAudioRecordingInteractorImpl

    @BeforeEach
    fun setUp() {
        sut = RenameAudioRecordingInteractorImpl(repository)
    }

    @Test
    fun `when execute with valid input expect rename recording in repository`() = runTest {
        val input = RenameAudioInput("id1", "newName")
        coEvery { repository.renameRecording("id1", "newName") } returns DataWrapper.Success(Unit)

        val result = sut.execute(input)

        assertEquals(DataWrapper.Success(Unit), result)
        coVerify { repository.renameRecording("id1", "newName") }
        confirmVerified(repository)
    }
}
