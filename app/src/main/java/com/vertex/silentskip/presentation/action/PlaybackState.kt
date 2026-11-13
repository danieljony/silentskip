package com.vertex.silentskip.presentation.action

data class PlaybackState(
    val playingId: String? = null,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L
)
