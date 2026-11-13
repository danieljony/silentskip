package com.vertex.silentskip.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.vertex.silentskip.MainActivity
import com.vertex.silentskip.R
import com.vertex.silentskip.common.BuildConstants
import com.vertex.silentskip.common.Logger
import com.vertex.silentskip.presentation.action.AudioRecordAction
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AudioRecordingService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null

    @Inject
    lateinit var audioRecordAction: AudioRecordAction

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        when (action) {
            ACTION_START -> {
                acquireWakeLock()
                val initialNotification = buildNotification(isPaused = false)
                if (BuildConstants.IS_AT_LEAST_UPSIDE_DOWN_CAKE) {
                    startForeground(
                        NOTIFICATION_ID,
                        initialNotification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                    )
                } else {
                    startForeground(NOTIFICATION_ID, initialNotification)
                }
            }
            ACTION_PAUSE -> {
                audioRecordAction.pauseRecording()
                val notification = buildNotification(isPaused = true)
                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, notification)
            }
            ACTION_RESUME -> {
                audioRecordAction.resumeRecording()
                val notification = buildNotification(isPaused = false)
                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, notification)
            }
            ACTION_STOP -> {
                audioRecordAction.stopRecording()
                stopForegroundAndSelf()
            }
        }

        return START_STICKY
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "SilentSkip:AudioRecordingWakeLock"
            ).apply {
                acquire(10 * 60 * 1000L /* 10 minutes timeout safety */)
            }
            Logger.i("WakeLock acquired for audio recording")
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
                Logger.i("WakeLock released")
            }
        }
        wakeLock = null
    }

    private fun stopForegroundAndSelf() {
        releaseWakeLock()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(isPaused: Boolean): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AudioRecordingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isPaused) {
            getString(R.string.notification_title_paused)
        } else {
            getString(R.string.notification_title_recording)
        }

        val text = if (isPaused) {
            getString(R.string.notification_text_paused)
        } else {
            getString(R.string.notification_text_recording)
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_mic_notification)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (isPaused) {
            val resumeIntent = Intent(this, AudioRecordingService::class.java).apply {
                action = ACTION_RESUME
            }
            val resumePendingIntent = PendingIntent.getService(
                this,
                2,
                resumeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.ic_media_play,
                getString(R.string.resume),
                resumePendingIntent
            )
        } else {
            val pauseIntent = Intent(this, AudioRecordingService::class.java).apply {
                action = ACTION_PAUSE
            }
            val pausePendingIntent = PendingIntent.getService(
                this,
                3,
                pauseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.ic_media_pause,
                getString(R.string.pause),
                pausePendingIntent
            )
        }

        builder.addAction(
            android.R.drawable.ic_delete,
            getString(R.string.stop),
            stopPendingIntent
        )

        return builder.build()
    }

    override fun onDestroy() {
        releaseWakeLock()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "silent_skip_recording_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.vertex.silentskip.action.START_RECORDING"
        const val ACTION_PAUSE = "com.vertex.silentskip.action.PAUSE_RECORDING"
        const val ACTION_RESUME = "com.vertex.silentskip.action.RESUME_RECORDING"
        const val ACTION_STOP = "com.vertex.silentskip.action.STOP_RECORDING"
    }
}
