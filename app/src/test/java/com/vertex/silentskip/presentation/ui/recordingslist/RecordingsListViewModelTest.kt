package com.vertex.silentskip.presentation.ui.recordingslist

import android.app.Activity
import com.vertex.silentskip.common.DataWrapper
import com.vertex.silentskip.domain.interactor.DeleteAudioRecordingInteractor
import com.vertex.silentskip.domain.interactor.GetAudioRecordingsInteractor
import com.vertex.silentskip.domain.interactor.RenameAudioRecordingInteractor
import com.vertex.silentskip.domain.model.AudioRecordModel
import com.vertex.silentskip.presentation.action.AdManagerAction
import com.vertex.silentskip.presentation.action.AudioPlaybackAction
import com.vertex.silentskip.presentation.action.PlaybackState
import com.vertex.silentskip.presentation.action.ShareAudioAction
import com.vertex.silentskip.presentation.model.RecordingsListUiState
import com.vertex.silentskip.utils.CoroutineExtension
import com.vertex.silentskip.utils.TestDispatcherProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class RecordingsListViewModelTest {

    private val getAudioRecordingsInteractor = mockk<GetAudioRecordingsInteractor>()
    private val renameAudioRecordingInteractor = mockk<RenameAudioRecordingInteractor>()
    private val deleteAudioRecordingInteractor = mockk<DeleteAudioRecordingInteractor>()
    private val audioPlaybackAction = mockk<AudioPlaybackAction>(relaxed = true)
    private val adManagerAction = mockk<AdManagerAction>(relaxed = true)
    private val shareAudioAction = mockk<ShareAudioAction>(relaxed = true)

    @RegisterExtension
    @JvmField
    val coroutineExtension = CoroutineExtension()

    private lateinit var sut: RecordingsListViewModel

    @BeforeEach
    fun setUp() {
        val recordings = listOf(AudioRecordModel("1", "rec1", "/path", 100L, 50L, "date", 1L))
        coEvery { getAudioRecordingsInteractor.execute() } returns DataWrapper.Success(recordings)
        every { audioPlaybackAction.playbackState } returns MutableStateFlow(PlaybackState())

        val dispatcherProvider = TestDispatcherProvider(coroutineExtension.dispatcher)
        sut = RecordingsListViewModel(
            getAudioRecordingsInteractor = getAudioRecordingsInteractor,
            renameAudioRecordingInteractor = renameAudioRecordingInteractor,
            deleteAudioRecordingInteractor = deleteAudioRecordingInteractor,
            audioPlaybackAction = audioPlaybackAction,
            adManagerAction = adManagerAction,
            shareAudioAction = shareAudioAction,
            dispatcherProvider = dispatcherProvider
        )
    }

    @Test
    fun `when loadRecordings succeeds expect state Success`() = runTest {
        sut.loadRecordings()
        coroutineExtension.dispatcher.scheduler.advanceUntilIdle()

        assertTrue(sut.uiState.value is RecordingsListUiState.Success)
    }

    @Test
    fun `when playRecording called expect audioPlaybackAction play invoked`() {
        val model = AudioRecordModel("1", "rec1", "/path", 100L, 50L, "date", 1L)
        sut.playRecording(model)

        verify { audioPlaybackAction.play("1", "/path") }
    }

    @Test
    fun `when pausePlayback and resumePlayback and seekPlayback called expect actions invoked`() {
        sut.pausePlayback()
        sut.resumePlayback()
        sut.seekPlayback(500L)

        verify { audioPlaybackAction.pause() }
        verify { audioPlaybackAction.resume() }
        verify { audioPlaybackAction.seekTo(500L) }
    }

    @Test
    fun `when showRenameDialog and dismissRenameDialog called expect state updated`() {
        val model = AudioRecordModel("1", "rec1", "/path", 100L, 50L, "date", 1L)
        sut.showRenameDialog(model)
        val state1 = sut.uiState.value as RecordingsListUiState.Success
        assertEquals(model, state1.renameTarget)

        sut.dismissRenameDialog()
        val state2 = sut.uiState.value as RecordingsListUiState.Success
        assertEquals(null, state2.renameTarget)
    }

    @Test
    fun `when shareRecording called expect shareAudioAction and adManagerAction marked`() {
        val activity = mockk<Activity>(relaxed = true)
        val model = AudioRecordModel("1", "rec1", "/path", 100L, 50L, "date", 1L)

        sut.shareRecording(activity, model)

        verify { shareAudioAction.share(activity, model) }
        verify { adManagerAction.markAudioShared() }
    }

    @Test
    fun `when checkPendingShareAd called expect adManagerAction checked`() {
        val activity = mockk<Activity>(relaxed = true)

        sut.checkPendingShareAd(activity)

        verify { adManagerAction.checkAndShowPendingShareAd(activity) }
    }
}
