package com.vertex.silentskip.presentation.action

import java.io.File

data class AudioRecordingState(
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedTimeMs: Long = 0L,
    val amplitudeNormalized: Float = 0f,
    val isSoundDetected: Boolean = false,
    val currentFile: File? = null
)
