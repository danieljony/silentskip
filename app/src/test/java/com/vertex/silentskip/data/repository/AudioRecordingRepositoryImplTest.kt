package com.vertex.silentskip.data.repository

import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.data.datasource.AudioStorageDataSource
import com.vertex.silentskip.domain.model.AudioRecordModel
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AudioRecordingRepositoryImplTest {

    private val dataSource = mockk<AudioStorageDataSource>()
    private lateinit var sut: AudioRecordingRepositoryImpl

    @BeforeEach
    fun setUp() {
        sut = AudioRecordingRepositoryImpl(dataSource)
    }

    @Test
    fun `when getRecordingsFlow expect flow from datasource`() {
        val flow = MutableStateFlow(emptyList<AudioRecordModel>())
        every { dataSource.recordingsFlow } returns flow

        val result = sut.getRecordingsFlow()

        assertEquals(flow, result)
        verify { dataSource.recordingsFlow }
    }

    @Test
    fun `when getRecordings expect recordings from datasource`() = runTest {
        val recordings = listOf(
            AudioRecordModel("id1", "rec1", "/path/1", 1000L, 500L, "01/01/2026", 123L)
        )
        every { dataSource.getRecordings() } returns recordings

        val result = sut.getRecordings()

        assertEquals(DataWrapper.Success(recordings), result)
        verify { dataSource.getRecordings() }
    }

    @Test
    fun `when renameRecording success expect Success`() = runTest {
        every { dataSource.renameRecording("id1", "new") } returns true

        val result = sut.renameRecording("id1", "new")

        assertEquals(DataWrapper.Success(Unit), result)
        verify { dataSource.renameRecording("id1", "new") }
    }

    @Test
    fun `when renameRecording failure expect Error`() = runTest {
        every { dataSource.renameRecording("id1", "new") } returns false

        val result = sut.renameRecording("id1", "new")

        assertTrue(result is DataWrapper.Error)
        verify { dataSource.renameRecording("id1", "new") }
    }

    @Test
    fun `when deleteRecording success expect Success`() = runTest {
        every { dataSource.deleteRecording("id1") } returns true

        val result = sut.deleteRecording("id1")

        assertEquals(DataWrapper.Success(Unit), result)
        verify { dataSource.deleteRecording("id1") }
    }

    @Test
    fun `when deleteRecording failure expect Error`() = runTest {
        every { dataSource.deleteRecording("id1") } returns false

        val result = sut.deleteRecording("id1")

        assertTrue(result is DataWrapper.Error)
        verify { dataSource.deleteRecording("id1") }
    }

    @Test
    fun `when saveRecording expect refreshRecordings`() = runTest {
        every { dataSource.refreshRecordings() } just runs
        val model = AudioRecordModel("1", "rec", "/path", 100L, 50L, "date", 1L)

        val result = sut.saveRecording(model)

        assertEquals(DataWrapper.Success(Unit), result)
        verify { dataSource.refreshRecordings() }
    }
}
