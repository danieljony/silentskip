package com.vertex.silentskip.presentation.model

import com.vertex.silentskip.domain.model.AppSettings

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Success(
        val settings: AppSettings
    ) : SettingsUiState

    data class Error(
        val message: String
    ) : SettingsUiState
}
