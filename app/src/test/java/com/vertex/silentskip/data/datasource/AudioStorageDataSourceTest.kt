package com.vertex.silentskip.data.datasource

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class AudioStorageDataSourceTest {

    private val context = mockk<Context>(relaxed = true)
    private lateinit var sut: AudioStorageDataSource

    @BeforeEach
    fun setUp() {
        val tmpDir = File(System.getProperty("java.io.tmpdir") ?: ".")
        every { context.filesDir } returns tmpDir
        sut = AudioStorageDataSource(context)
    }

    @Test
    fun `when generateNewRecordingFile expect valid file`() {
        val file = sut.generateNewRecordingFile("wav")
        assertNotNull(file)
    }

    @Test
    fun `when deleteRecording with non-existent id expect false`() {
        val result = sut.deleteRecording("non_existent_id")
        assertFalse(result)
    }
}
