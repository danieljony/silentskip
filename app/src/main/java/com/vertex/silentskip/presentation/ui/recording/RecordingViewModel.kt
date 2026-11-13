package com.vertex.silentskip.presentation.ui.recording

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vertex.silentskip.common.DispatcherProvider
import com.vertex.silentskip.domain.interactor.GetAppSettingsInteractor
import com.vertex.silentskip.domain.model.AppSettings
import com.vertex.silentskip.presentation.action.AdManagerAction
import com.vertex.silentskip.presentation.action.AudioRecordAction
import com.vertex.silentskip.presentation.model.RecordingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordingViewModel @Inject constructor(
    val audioRecordAction: AudioRecordAction,
    val getAppSettingsInteractor: GetAppSettingsInteractor,
    val adManagerAction: AdManagerAction,
    val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecordingUiState>(RecordingUiState.Idle)
    val uiState: StateFlow<RecordingUiState> = _uiState.asStateFlow()

    private val currentSettings = MutableStateFlow(AppSettings())

    init {
        observeSettingsAndRecording()
    }

    fun startRecording() {
        viewModelScope.launch(dispatcherProvider.io) {
            audioRecordAction.startRecording()
        }
    }

    fun pauseRecording() {
        viewModelScope.launch(dispatcherProvider.io) {
            audioRecordAction.pauseRecording()
        }
    }

    fun resumeRecording() {
        viewModelScope.launch(dispatcherProvider.io) {
            audioRecordAction.resumeRecording()
        }
    }

    fun stopRecording() {
        viewModelScope.launch(dispatcherProvider.io) {
            audioRecordAction.stopRecording()
        }
    }

    private fun observeSettingsAndRecording() {
        viewModelScope.launch(dispatcherProvider.io) {
            val settingsResult = getAppSettingsInteractor.execute()
            settingsResult.onSuccess { settings ->
                currentSettings.value = settings
                adManagerAction.onSettingsChanged(settings.isPremiumSubscriber)
            }

            combine(
                audioRecordAction.recordingState,
                currentSettings
            ) { recState, settings ->
                RecordingUiState.Active(
                    recordingState = recState,
                    settings = settings
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
