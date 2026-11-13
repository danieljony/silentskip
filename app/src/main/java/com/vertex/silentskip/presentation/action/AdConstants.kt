package com.vertex.silentskip.presentation.action

import com.vertex.silentskip.BuildConfig

object AdConstants {

    const val MONTHLY_SUBSCRIPTION_ID = "silentskip_monthly"
    const val ANNUAL_SUBSCRIPTION_ID = "silentskip_annual"

    val BANNER_AD_UNIT_ID: String
        get() = if (BuildConfig.DEBUG || BuildConfig.ADMOB_USE_TEST_ADS) {
            "ca-app-pub-3940256099942544/6300978111" // Test banner ID
        } else {
            BuildConfig.ADMOB_BANNER_ID
        }

    val INTERSTITIAL_AD_UNIT_ID: String
        get() = if (BuildConfig.DEBUG || BuildConfig.ADMOB_USE_TEST_ADS) {
            "ca-app-pub-3940256099942544/1033173712" // Test interstitial ID
        } else {
            BuildConfig.ADMOB_INTERSTITIAL_ID
        }
}
