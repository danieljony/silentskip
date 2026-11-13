package com.vertex.silentskip.presentation.ui.recordingslist

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vertex.silentskip.common.DispatcherProvider
import com.vertex.silentskip.domain.interactor.DeleteAudioRecordingInteractor
import com.vertex.silentskip.domain.interactor.GetAudioRecordingsInteractor
import com.vertex.silentskip.domain.interactor.RenameAudioRecordingInteractor
import com.vertex.silentskip.domain.model.AudioRecordModel
import com.vertex.silentskip.domain.model.RenameAudioInput
import com.vertex.silentskip.presentation.action.AdManagerAction
import com.vertex.silentskip.presentation.action.AudioPlaybackAction
import com.vertex.silentskip.presentation.action.ShareAudioAction
import com.vertex.silentskip.presentation.model.RecordingsListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordingsListViewModel @Inject constructor(
    val getAudioRecordingsInteractor: GetAudioRecordingsInteractor,
    val renameAudioRecordingInteractor: RenameAudioRecordingInteractor,
    val deleteAudioRecordingInteractor: DeleteAudioRecordingInteractor,
    val audioPlaybackAction: AudioPlaybackAction,
    val adManagerAction: AdManagerAction,
    val shareAudioAction: ShareAudioAction,
    val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecordingsListUiState>(RecordingsListUiState.Loading)
    val uiState: StateFlow<RecordingsListUiState> = _uiState.asStateFlow()

    private var currentRecordings: List<AudioRecordModel> = emptyList()
    private var renameTargetModel: AudioRecordModel? = null

    init {
        loadRecordings()
        observePlaybackState()
    }

    fun loadRecordings() {
        viewModelScope.launch(dispatcherProvider.io) {
            _uiState.value = RecordingsListUiState.Loading
            val result = getAudioRecordingsInteractor.execute()
            result.onSuccess { list ->
                currentRecordings = list
                updateSuccessState()
            }.onError { error ->
                _uiState.value = RecordingsListUiState.Error(
                    error.message ?: "Erro ao carregar gravações"
                )
            }
        }
    }

    fun playRecording(recording: AudioRecordModel) {
        audioPlaybackAction.play(recording.id, recording.filePath)
    }

    fun shareRecording(activity: Activity, recording: AudioRecordModel) {
        shareAudioAction.share(activity, recording)
        adManagerAction.markAudioShared()
    }

    fun checkPendingShareAd(activity: Activity) {
        adManagerAction.checkAndShowPendingShareAd(activity)
    }

    fun pausePlayback() {
        audioPlaybackAction.pause()
    }

    fun resumePlayback() {
        audioPlaybackAction.resume()
    }

    fun seekPlayback(positionMs: Long) {
        audioPlaybackAction.seekTo(positionMs)
    }

    fun showRenameDialog(recording: AudioRecordModel) {
        renameTargetModel = recording
        updateSuccessState()
    }

    fun dismissRenameDialog() {
        renameTargetModel = null
        updateSuccessState()
    }

    fun confirmRename(activity: Activity, recordingId: String, newName: String) {
        viewModelScope.launch(dispatcherProvider.io) {
            val input = RenameAudioInput(id = recordingId, newName = newName)
            val result = renameAudioRecordingInteractor.execute(input)
            result.onSuccess {
                adManagerAction.requestRenameWithInterstitial(activity) {
                    renameTargetModel = null
                    loadRecordings()
                }
            }.onError { error ->
                _uiState.value = RecordingsListUiState.Error(
                    error.message ?: "Erro ao renomear arquivo"
                )
            }
        }
    }

    fun requestDeleteRecording(activity: Activity, recording: AudioRecordModel) {
        adManagerAction.requestDeleteWithInterstitial(activity) {
            performDelete(recording.id)
        }
    }

    private fun performDelete(recordingId: String) {
        viewModelScope.launch(dispatcherProvider.io) {
            if (audioPlaybackAction.playbackState.value.playingId == recordingId) {
                audioPlaybackAction.stop()
            }
            val result = deleteAudioRecordingInteractor.execute(recordingId)
            result.onSuccess {
                loadRecordings()
            }.onError { error ->
                _uiState.value = RecordingsListUiState.Error(
                    error.message ?: "Erro ao deletar arquivo"
                )
            }
        }
    }

    private fun observePlaybackState() {
        viewModelScope.launch(dispatcherProvider.main) {
            audioPlaybackAction.playbackState.collect {
                updateSuccessState()
            }
        }
    }

    private fun updateSuccessState() {
        _uiState.value = RecordingsListUiState.Success(
            recordings = currentRecordings,
            playbackState = audioPlaybackAction.playbackState.value,
            renameTarget = renameTargetModel
        )
    }

    override fun onCleared() {
        super.onCleared()
        audioPlaybackAction.stop()
    }
}
