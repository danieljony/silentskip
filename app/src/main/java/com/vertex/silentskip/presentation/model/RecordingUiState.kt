package com.vertex.silentskip.presentation.model

import com.vertex.silentskip.domain.model.AppSettings
import com.vertex.silentskip.presentation.action.AudioRecordingState

sealed interface RecordingUiState {
    data object Idle : RecordingUiState

    data class Active(
        val recordingState: AudioRecordingState,
        val settings: AppSettings
    ) : RecordingUiState

    data class Error(
        val message: String
    ) : RecordingUiState
}
