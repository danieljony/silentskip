package com.vertex.silentskip.presentation.ui.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vertex.silentskip.common.DispatcherProvider
import com.vertex.silentskip.data.billing.BillingManager
import com.vertex.silentskip.domain.interactor.GetAppSettingsInteractor
import com.vertex.silentskip.domain.interactor.SaveAppSettingsInteractor
import com.vertex.silentskip.domain.interactor.ToggleSubscriptionInteractor
import com.vertex.silentskip.domain.model.AppSettings
import com.vertex.silentskip.presentation.action.AdManagerAction
import com.vertex.silentskip.presentation.model.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val getAppSettingsInteractor: GetAppSettingsInteractor,
    val saveAppSettingsInteractor: SaveAppSettingsInteractor,
    val toggleSubscriptionInteractor: ToggleSubscriptionInteractor,
    val adManagerAction: AdManagerAction,
    val billingManager: BillingManager,
    val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch(dispatcherProvider.io) {
            _uiState.value = SettingsUiState.Loading
            val result = getAppSettingsInteractor.execute()
            result.onSuccess { settings ->
                _uiState.value = SettingsUiState.Success(settings)
                adManagerAction.onSettingsChanged(settings.isPremiumSubscriber)
            }.onError { error ->
                _uiState.value = SettingsUiState.Error(
                    error.message ?: "Erro ao carregar configurações"
                )
            }
        }
    }

    fun toggleSoundDetection(enabled: Boolean) {
        val current = (_uiState.value as? SettingsUiState.Success)?.settings ?: return
        val updated = current.copy(isSoundDetectionEnabled = enabled)
        saveSettings(updated)
    }

    fun toggleSubscription(isSubscribed: Boolean) {
        viewModelScope.launch(dispatcherProvider.io) {
            val result = toggleSubscriptionInteractor.execute(isSubscribed)
            result.onSuccess { settings ->
                _uiState.value = SettingsUiState.Success(settings)
                adManagerAction.onSettingsChanged(settings.isPremiumSubscriber)
            }.onError { error ->
                _uiState.value = SettingsUiState.Error(
                    error.message ?: "Erro ao alterar assinatura"
                )
            }
        }
    }

    fun subscribeMonthly(activity: Activity) {
        billingManager.monthlyProductDetails.value?.let {
            billingManager.launchBillingFlow(activity, it)
        }
    }

    fun subscribeAnnual(activity: Activity) {
        billingManager.annualProductDetails.value?.let {
            billingManager.launchBillingFlow(activity, it)
        }
    }

    private fun saveSettings(settings: AppSettings) {
        viewModelScope.launch(dispatcherProvider.io) {
            _uiState.value = SettingsUiState.Success(settings)
            saveAppSettingsInteractor.execute(settings)
            adManagerAction.onSettingsChanged(settings.isPremiumSubscriber)
        }
    }
}
