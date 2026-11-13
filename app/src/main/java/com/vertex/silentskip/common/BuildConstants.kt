package com.vertex.silentskip.common

import android.os.Build
import com.vertex.silentskip.BuildConfig

object BuildConstants {
    val IS_DEBUG: Boolean = BuildConfig.DEBUG
    val IS_AT_LEAST_O: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    val IS_AT_LEAST_TIRAMISU: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val IS_AT_LEAST_UPSIDE_DOWN_CAKE: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
}
