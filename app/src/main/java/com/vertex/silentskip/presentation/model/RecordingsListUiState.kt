package com.vertex.silentskip.presentation.model

import com.vertex.silentskip.domain.model.AudioRecordModel
import com.vertex.silentskip.presentation.action.PlaybackState

sealed interface RecordingsListUiState {
    data object Loading : RecordingsListUiState

    data class Success(
        val recordings: List<AudioRecordModel>,
        val playbackState: PlaybackState,
        val renameTarget: AudioRecordModel? = null
    ) : RecordingsListUiState

    data class Error(
        val message: String
    ) : RecordingsListUiState
}
