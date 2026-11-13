package com.vertex.silentskip.presentation.action

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.vertex.silentskip.common.Logger
import com.vertex.silentskip.data.datasource.AppSettingsDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class AdManagerAction @Inject constructor(
    private val appSettingsDataSource: AppSettingsDataSource
) {

    private val _adState = MutableStateFlow(
        AdState(showBannerAd = !appSettingsDataSource.getSettings().isPremiumSubscriber)
    )
    val adState: StateFlow<AdState> = _adState.asStateFlow()

    private var interstitialAd: InterstitialAd? = null
    private var isLoadingAd = false
    private var hasPendingShareAd = false

    fun preloadInterstitialAd(context: Context) {
        if (interstitialAd != null || isLoadingAd) return
        val isPremium = appSettingsDataSource.getSettings().isPremiumSubscriber
        if (isPremium) return

        isLoadingAd = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            AdConstants.INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoadingAd = false
                    Logger.i("InterstitialAd preloaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoadingAd = false
                    Logger.e("InterstitialAd failed to load: ${error.message}")
                }
            }
        )
    }

    fun onSettingsChanged(isPremium: Boolean) {
        _adState.value = _adState.value.copy(
            showBannerAd = !isPremium
        )
    }

    fun requestDeleteWithInterstitial(activity: Activity, onDeleteConfirmed: () -> Unit) {
        showInterstitialOrRun(activity, onDeleteConfirmed)
    }

    fun requestRenameWithInterstitial(activity: Activity, onRenameConfirmed: () -> Unit) {
        showInterstitialOrRun(activity, onRenameConfirmed)
    }

    fun markAudioShared() {
        hasPendingShareAd = true
    }

    fun checkAndShowPendingShareAd(activity: Activity) {
        val isPremium = appSettingsDataSource.getSettings().isPremiumSubscriber
        if (isPremium) {
            hasPendingShareAd = false
            return
        }
        if (hasPendingShareAd) {
            hasPendingShareAd = false
            showInterstitialOrRun(activity) {}
        }
    }

    private fun showInterstitialOrRun(activity: Activity, onFinished: () -> Unit) {
        val isPremium = appSettingsDataSource.getSettings().isPremiumSubscriber
        if (isPremium) {
            onFinished()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    preloadInterstitialAd(activity)
                    onFinished()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    preloadInterstitialAd(activity)
                    onFinished()
                }
            }
            ad.show(activity)
        } else {
            preloadInterstitialAd(activity)
            onFinished()
        }
    }
}
