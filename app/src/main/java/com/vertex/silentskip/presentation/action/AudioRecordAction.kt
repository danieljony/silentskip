package com.vertex.silentskip.presentation.action

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.audiofx.AcousticEchoCanceler
import android.media.audiofx.NoiseSuppressor
import androidx.core.content.ContextCompat
import com.vertex.silentskip.common.BuildConstants
import com.vertex.silentskip.common.DispatcherProvider
import com.vertex.silentskip.common.Logger
import com.vertex.silentskip.data.datasource.AppSettingsDataSource
import com.vertex.silentskip.data.datasource.AudioStorageDataSource
import com.vertex.silentskip.domain.model.AudioRecordModel
import com.vertex.silentskip.service.AudioRecordingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.RandomAccessFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val DATE_PATTERN = "dd-MM-yyyy - HH-mm"

class AudioRecordAction @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val audioStorageDataSource: AudioStorageDataSource,
    private val appSettingsDataSource: AppSettingsDataSource,
    dispatcherProvider: DispatcherProvider
) {

    private val scope = CoroutineScope(dispatcherProvider.io)

    private var audioRecord: AudioRecord? = null
    private var randomAccessFile: RandomAccessFile? = null

    private var noiseSuppressor: NoiseSuppressor? = null
    private var echoCanceler: AcousticEchoCanceler? = null

    private var recordingJob: Job? = null

    private var currentFile: File? = null
    private var totalPcmBytesWritten: Long = 0L
    private var lastDetectionTimeMs: Long = 0L
    private var isWritingToDisk: Boolean = false
    private var isManuallyPaused: Boolean = false

    private val sampleRate = 16000
    private val bufferWindowMs = 2000L // 2-second pre-roll and post-roll buffer
    private val circularBufferCapacity = sampleRate * 2 // 32,000 shorts = 2 seconds at 16kHz MONO
    private val circularBuffer = CircularShortBuffer(circularBufferCapacity)

    private val _recordingState = MutableStateFlow(AudioRecordingState())
    val recordingState: StateFlow<AudioRecordingState> = _recordingState.asStateFlow()

    fun startRecording(): File? {
        if (_recordingState.value.isRecording) return _recordingState.value.currentFile

        val outputFile = audioStorageDataSource.generateNewRecordingFile("wav")

        return try {
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

            if (minBufferSize <= 0) return null

            @Suppress("MissingPermission")
            val record = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                minBufferSize * 2
            )

            if (record.state != AudioRecord.STATE_INITIALIZED) {
                record.release()
                return null
            }

            setupAudioEffects(record.audioSessionId)

            val raf = RandomAccessFile(outputFile, "rw")
            raf.setLength(0)
            raf.write(ByteArray(44)) // 44-byte placeholder WAV header

            audioRecord = record
            randomAccessFile = raf
            currentFile = outputFile
            totalPcmBytesWritten = 0L
            lastDetectionTimeMs = System.currentTimeMillis()
            isWritingToDisk = false
            isManuallyPaused = false
            circularBuffer.clear()

            record.startRecording()

            _recordingState.value = AudioRecordingState(
                isRecording = true,
                isPaused = false,
                elapsedTimeMs = 0L,
                currentFile = outputFile
            )

            startRecordingLoop(minBufferSize)
            startServiceAction(AudioRecordingService.ACTION_START)

            outputFile
        } catch (e: Exception) {
            Logger.e("Failed to start audio recording", e)
            stopRecording()
            null
        }
    }

    fun pauseRecording() {
        val currentState = _recordingState.value
        if (!currentState.isRecording || isManuallyPaused) return
        isManuallyPaused = true
        _recordingState.value = currentState.copy(isPaused = true)
        startServiceAction(AudioRecordingService.ACTION_PAUSE)
    }

    fun resumeRecording() {
        val currentState = _recordingState.value
        if (!currentState.isRecording || !isManuallyPaused) return
        isManuallyPaused = false
        lastDetectionTimeMs = System.currentTimeMillis()
        _recordingState.value = currentState.copy(isPaused = false)
        startServiceAction(AudioRecordingService.ACTION_RESUME)
    }

    fun stopRecording(): AudioRecordModel? {
        recordingJob?.cancel()
        recordingJob = null

        val file = currentFile

        releaseAudioEffects()

        try {
            audioRecord?.apply {
                try {
                    stop()
                } catch (e: Exception) {
                    Logger.e("Error stopping AudioRecord", e)
                }
                release()
                startServiceAction(AudioRecordingService.ACTION_STOP)
            }
        } catch (e: Exception) {
            Logger.e("Error releasing AudioRecord", e)
        } finally {
            audioRecord = null
        }

        try {
            randomAccessFile?.apply {
                if (totalPcmBytesWritten > 0) {
                    writeWavHeader(this, totalPcmBytesWritten, sampleRate, 1)
                }
                close()
            }
        } catch (e: Exception) {
            Logger.e("Error closing WAV file", e)
        } finally {
            randomAccessFile = null
        }

        val recordedDurationMs = (totalPcmBytesWritten * 1000L) / (sampleRate * 2L)

        _recordingState.value = AudioRecordingState()
        circularBuffer.clear()
        isWritingToDisk = false
        isManuallyPaused = false

        if (file != null && file.exists() && file.length() > 44) {
            audioStorageDataSource.refreshRecordings()
            val dateFormatter = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
            return AudioRecordModel(
                id = file.absolutePath,
                name = file.nameWithoutExtension,
                filePath = file.absolutePath,
                durationMs = recordedDurationMs,
                sizeBytes = file.length(),
                createdAtFormatted = dateFormatter.format(Date()),
                timestamp = file.lastModified()
            )
        }
        return null
    }

    private fun startServiceAction(actionName: String) {
        try {
            val intent = Intent(context, AudioRecordingService::class.java).apply {
                action = actionName
            }
            if (BuildConstants.IS_AT_LEAST_O && actionName == AudioRecordingService.ACTION_START) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            Logger.e("Failed to send action $actionName to AudioRecordingService", e)
        }
    }

    private fun startRecordingLoop(bufferSize: Int) {
        recordingJob?.cancel()
        recordingJob = scope.launch {
            val record = audioRecord ?: return@launch
            val raf = randomAccessFile ?: return@launch
            val readBuffer = ShortArray(bufferSize)

            while (isActive && _recordingState.value.isRecording) {
                if (!isManuallyPaused) {
                    val readSize = record.read(readBuffer, 0, readBuffer.size)
                    if (readSize > 0) {
                        var maxAmp = 0
                        for (i in 0 until readSize) {
                            val sample = readBuffer[i].toInt()
                            val absSample = if (sample < 0) -sample else sample
                            if (absSample > maxAmp) {
                                maxAmp = absSample
                            }
                        }

                        val normalized = (maxAmp / 32767f).coerceIn(0f, 1f)
                        val soundDetected = normalized > 0.03f

                        val settings = appSettingsDataSource.getSettings()
                        val soundEnabled = settings.isSoundDetectionEnabled

                        val now = System.currentTimeMillis()

                        if (!soundEnabled) {
                            writeShortsToStream(readBuffer, readSize, raf)
                            totalPcmBytesWritten += readSize * 2L
                            lastDetectionTimeMs = now
                            isWritingToDisk = true
                        } else if (soundDetected) {
                            lastDetectionTimeMs = now
                            if (!isWritingToDisk) {
                                isWritingToDisk = true
                                val preRoll = circularBuffer.readAll()
                                if (preRoll.isNotEmpty()) {
                                    writeShortsToStream(preRoll, preRoll.size, raf)
                                    totalPcmBytesWritten += preRoll.size * 2L
                                    circularBuffer.clear()
                                }
                            }
                            writeShortsToStream(readBuffer, readSize, raf)
                            totalPcmBytesWritten += readSize * 2L
                            circularBuffer.push(readBuffer, readSize)
                        } else {
                            circularBuffer.push(readBuffer, readSize)
                            val timeSinceLastDetection = now - lastDetectionTimeMs
                            if (timeSinceLastDetection < bufferWindowMs) {
                                writeShortsToStream(readBuffer, readSize, raf)
                                totalPcmBytesWritten += readSize * 2L
                            } else {
                                isWritingToDisk = false
                            }
                        }

                        val currentDurationMs = (totalPcmBytesWritten * 1000L) / (sampleRate * 2L)

                        _recordingState.value = _recordingState.value.copy(
                            elapsedTimeMs = currentDurationMs,
                            amplitudeNormalized = normalized,
                            isSoundDetected = soundDetected
                        )
                    }
                }
            }
        }
    }

    private fun setupAudioEffects(sessionId: Int) {
        try {
            if (NoiseSuppressor.isAvailable()) {
                noiseSuppressor = NoiseSuppressor.create(sessionId)?.apply {
                    enabled = true
                }
                Logger.i("Hardware NoiseSuppressor enabled on audioSessionId $sessionId")
            } else {
                Logger.w("Hardware NoiseSuppressor not available on this device")
            }
        } catch (e: Exception) {
            Logger.e("Failed to enable NoiseSuppressor", e)
        }

        try {
            if (AcousticEchoCanceler.isAvailable()) {
                echoCanceler = AcousticEchoCanceler.create(sessionId)?.apply {
                    enabled = true
                }
                Logger.i("Hardware AcousticEchoCanceler enabled on audioSessionId $sessionId")
            } else {
                Logger.w("Hardware AcousticEchoCanceler not available on this device")
            }
        } catch (e: Exception) {
            Logger.e("Failed to enable AcousticEchoCanceler", e)
        }
    }

    private fun releaseAudioEffects() {
        try {
            noiseSuppressor?.apply {
                enabled = false
                release()
            }
        } catch (e: Exception) {
            Logger.e("Error releasing NoiseSuppressor", e)
        } finally {
            noiseSuppressor = null
        }

        try {
            echoCanceler?.apply {
                enabled = false
                release()
            }
        } catch (e: Exception) {
            Logger.e("Error releasing AcousticEchoCanceler", e)
        } finally {
            echoCanceler = null
        }
    }

    private fun writeShortsToStream(shorts: ShortArray, length: Int, raf: RandomAccessFile) {
        val bytes = ByteArray(length * 2)
        for (i in 0 until length) {
            val value = shorts[i].toInt()
            bytes[i * 2] = (value and 0xff).toByte()
            bytes[i * 2 + 1] = ((value shr 8) and 0xff).toByte()
        }
        raf.write(bytes)
    }

    private fun writeWavHeader(raf: RandomAccessFile, totalAudioLen: Long, sampleRate: Int, channels: Int) {
        val totalDataLen = totalAudioLen + 36
        val byteRate = sampleRate * channels * 2
        val header = ByteArray(44)

        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * 2).toByte()
        header[33] = 0
        header[34] = 16
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        raf.seek(0)
        raf.write(header)
    }

    private class CircularShortBuffer(val capacity: Int) {
        private val buffer = ShortArray(capacity)
        private var head = 0
        private var count = 0

        @Synchronized
        fun push(data: ShortArray, length: Int) {
            val sizeToPush = length.coerceAtMost(capacity)
            for (i in 0 until sizeToPush) {
                buffer[head] = data[i]
                head = (head + 1) % capacity
                if (count < capacity) {
                    count++
                }
            }
        }

        @Synchronized
        fun readAll(): ShortArray {
            val result = ShortArray(count)
            if (count == 0) return result
            val start = if (count < capacity) 0 else head
            for (i in 0 until count) {
                result[i] = buffer[(start + i) % capacity]
            }
            return result
        }

        @Synchronized
        fun clear() {
            head = 0
            count = 0
        }
    }
}
