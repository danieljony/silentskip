package com.vertex.silentskip.presentation.ui.recording

import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.domain.interactor.GetAppSettingsInteractor
import com.vertex.silentskip.domain.model.AppSettings
import com.vertex.silentskip.presentation.action.AdManagerAction
import com.vertex.silentskip.presentation.action.AudioRecordAction
import com.vertex.silentskip.presentation.action.AudioRecordingState
import com.vertex.silentskip.utils.CoroutineExtension
import com.vertex.silentskip.utils.TestDispatcherProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class RecordingViewModelTest {

    private val audioRecordAction = mockk<AudioRecordAction>(relaxed = true)
    private val getAppSettingsInteractor = mockk<GetAppSettingsInteractor>()
    private val adManagerAction = mockk<AdManagerAction>(relaxed = true)

    @RegisterExtension
    @JvmField
    val coroutineExtension = CoroutineExtension()

    private lateinit var sut: RecordingViewModel

    @BeforeEach
    fun setUp() {
        val settings = AppSettings(isSoundDetectionEnabled = true, isPremiumSubscriber = false)
        coEvery { getAppSettingsInteractor.execute() } returns DataWrapper.Success(settings)
        every { audioRecordAction.recordingState } returns MutableStateFlow(AudioRecordingState())

        val dispatcherProvider = TestDispatcherProvider(coroutineExtension.dispatcher)
        sut = RecordingViewModel(
            audioRecordAction = audioRecordAction,
            getAppSettingsInteractor = getAppSettingsInteractor,
            adManagerAction = adManagerAction,
            dispatcherProvider = dispatcherProvider
        )
    }

    @Test
    fun `when startRecording expect audioRecordAction startRecording invoked`() = runTest {
        sut.startRecording()
        coroutineExtension.dispatcher.scheduler.advanceUntilIdle()

        coVerify { audioRecordAction.startRecording() }
    }

    @Test
    fun `when pauseRecording expect audioRecordAction pauseRecording invoked`() = runTest {
        sut.pauseRecording()
        coroutineExtension.dispatcher.scheduler.advanceUntilIdle()

        coVerify { audioRecordAction.pauseRecording() }
    }

    @Test
    fun `when resumeRecording expect audioRecordAction resumeRecording invoked`() = runTest {
        sut.resumeRecording()
        coroutineExtension.dispatcher.scheduler.advanceUntilIdle()

        coVerify { audioRecordAction.resumeRecording() }
    }

    @Test
    fun `when stopRecording expect audioRecordAction stopRecording invoked`() = runTest {
        sut.stopRecording()
        coroutineExtension.dispatcher.scheduler.advanceUntilIdle()

        coVerify { audioRecordAction.stopRecording() }
    }
}
