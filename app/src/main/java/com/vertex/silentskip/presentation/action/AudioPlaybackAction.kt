package com.vertex.silentskip.presentation.action

import android.media.MediaPlayer
import com.vertex.silentskip.common.DispatcherProvider
import com.vertex.silentskip.common.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class AudioPlaybackAction @Inject constructor(
    dispatcherProvider: DispatcherProvider
) {

    private val scope = CoroutineScope(dispatcherProvider.main)
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    fun play(id: String, filePath: String) {
        val currentState = _playbackState.value
        if (currentState.playingId == id && currentState.isPaused) {
            resume()
            return
        }

        stop()

        try {
            val player = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                start()
            }
            mediaPlayer = player

            _playbackState.value = PlaybackState(
                playingId = id,
                isPlaying = true,
                isPaused = false,
                currentPositionMs = 0L,
                durationMs = player.duration.toLong()
            )

            player.setOnCompletionListener {
                stop()
            }

            startProgressTracker()
        } catch (e: Exception) {
            Logger.e("Failed to play audio file", e)
            stop()
        }
    }

    fun pause() {
        val currentState = _playbackState.value
        if (currentState.isPlaying) {
            try {
                mediaPlayer?.pause()
                _playbackState.value = currentState.copy(
                    isPlaying = false,
                    isPaused = true
                )
            } catch (e: Exception) {
                Logger.e("Error pausing playback", e)
            }
        }
    }

    fun resume() {
        val currentState = _playbackState.value
        if (currentState.isPaused) {
            try {
                mediaPlayer?.start()
                _playbackState.value = currentState.copy(
                    isPlaying = true,
                    isPaused = false
                )
                startProgressTracker()
            } catch (e: Exception) {
                Logger.e("Error resuming playback", e)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        try {
            mediaPlayer?.seekTo(positionMs.toInt())
            _playbackState.value = _playbackState.value.copy(
                currentPositionMs = positionMs
            )
        } catch (e: Exception) {
            Logger.e("Error seeking playback", e)
        }
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null

        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            Logger.e("Error stopping playback", e)
        } finally {
            mediaPlayer = null
        }

        _playbackState.value = PlaybackState()
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _playbackState.value.isPlaying) {
                try {
                    val pos = mediaPlayer?.currentPosition?.toLong() ?: 0L
                    _playbackState.value = _playbackState.value.copy(
                        currentPositionMs = pos
                    )
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Logger.e("Error tracking playback position", e)
                }
                delay(200.milliseconds)
            }
        }
    }
}
