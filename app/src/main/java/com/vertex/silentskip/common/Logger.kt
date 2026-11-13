package com.vertex.silentskip.common

import android.util.Log

object Logger {
    private const val DEFAULT_TAG = "SilentSkip"

    fun d(message: String) {
        if (BuildConstants.IS_DEBUG) {
            Log.d(DEFAULT_TAG, message)
        }
    }

    fun i(message: String) {
        Log.i(DEFAULT_TAG, message)
    }

    fun w(message: String) {
        Log.w(DEFAULT_TAG, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        Log.e(DEFAULT_TAG, message, throwable)
    }
}
