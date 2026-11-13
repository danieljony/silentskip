package com.vertex.silentskip.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.vertex.silentskip.R
import com.vertex.silentskip.presentation.navigation.Screen

@Composable
fun SilentSkipBottomBar(
    currentRoute: String?,
    onNavigateToRoute: (String) -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Screen.Recording.route,
            onClick = { onNavigateToRoute(Screen.Recording.route) },
            icon = { Icon(Icons.Default.Mic, contentDescription = stringResource(R.string.nav_recording)) },
            label = { Text(stringResource(R.string.nav_recording)) }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.RecordingsList.route,
            onClick = { onNavigateToRoute(Screen.RecordingsList.route) },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(R.string.nav_recordings)) },
            label = { Text(stringResource(R.string.nav_recordings)) }
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Settings.route,
            onClick = { onNavigateToRoute(Screen.Settings.route) },
            icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings)) },
            label = { Text(stringResource(R.string.nav_settings)) }
        )
    }
}
