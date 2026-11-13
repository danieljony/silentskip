package com.vertex.silentskip.presentation.ui.recording

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vertex.silentskip.R
import com.vertex.silentskip.common.BuildConstants
import com.vertex.silentskip.presentation.action.AdState
import com.vertex.silentskip.presentation.action.AudioRecordingState
import com.vertex.silentskip.presentation.model.RecordingUiState
import com.vertex.silentskip.presentation.ui.components.AdBannerView
import com.vertex.silentskip.presentation.ui.components.SoundVisualizer

@Composable
fun RecordingScreen(
    viewModel: RecordingViewModel,
    adState: AdState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val requiredPermissions = remember {
        if (BuildConstants.IS_AT_LEAST_TIRAMISU) {
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun checkPermissionsGranted(): Boolean {
        return requiredPermissions.all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
    }

    var hasPermissions by remember {
        mutableStateOf(checkPermissionsGranted())
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        hasPermissions = checkPermissionsGranted()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (adState.showBannerAd) {
                AdBannerView()
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!hasPermissions) {
                PermissionRequestContent(
                    onRequestPermission = {
                        permissionLauncher.launch(requiredPermissions)
                    }
                )
            } else {
                when (val state = uiState) {
                    is RecordingUiState.Idle -> {
                        RecordingContent(
                            recordingState = null,
                            isSoundDetectionEnabled = true,
                            onStartRecording = { viewModel.startRecording() },
                            onPauseRecording = { viewModel.pauseRecording() },
                            onResumeRecording = { viewModel.resumeRecording() },
                            onStopRecording = { viewModel.stopRecording() }
                        )
                    }
                    is RecordingUiState.Active -> {
                        RecordingContent(
                            recordingState = state.recordingState,
                            isSoundDetectionEnabled = state.settings.isSoundDetectionEnabled,
                            onStartRecording = { viewModel.startRecording() },
                            onPauseRecording = { viewModel.pauseRecording() },
                            onResumeRecording = { viewModel.resumeRecording() },
                            onStopRecording = { viewModel.stopRecording() }
                        )
                    }
                    is RecordingUiState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRequestContent(
    onRequestPermission: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.mic_permission_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.mic_permission_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRequestPermission) {
                Text(stringResource(R.string.grant_permission))
            }
        }
    }
}

@Composable
private fun RecordingContent(
    recordingState: AudioRecordingState?,
    isSoundDetectionEnabled: Boolean,
    onStartRecording: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    val isRecording = recordingState?.isRecording == true
    val isPaused = recordingState?.isPaused == true
    val elapsedTimeMs = recordingState?.elapsedTimeMs ?: 0L

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.recorder_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatDuration(elapsedTimeMs),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when {
                    isRecording && !isPaused -> stringResource(R.string.recording_status)
                    isRecording && isPaused -> stringResource(R.string.recording_paused)
                    else -> stringResource(R.string.ready_to_record)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SoundVisualizer(
            amplitudeNormalized = recordingState?.amplitudeNormalized ?: 0f,
            isSoundDetected = recordingState?.isSoundDetected == true,
            isSoundDetectionEnabled = isSoundDetectionEnabled
        )

        Box(
            modifier = Modifier
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!isRecording) {
                LargeFloatingActionButton(
                    onClick = onStartRecording,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Iniciar Gravação",
                        modifier = Modifier.size(36.dp)
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPaused) {
                        FloatingActionButton(
                            onClick = onResumeRecording,
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Retomar")
                        }
                    } else {
                        FloatingActionButton(
                            onClick = onPauseRecording,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = "Pausar")
                        }
                    }

                    LargeFloatingActionButton(
                        onClick = onStopRecording,
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Parar Gravação",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = (millis / (1000 * 60 * 60))
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
