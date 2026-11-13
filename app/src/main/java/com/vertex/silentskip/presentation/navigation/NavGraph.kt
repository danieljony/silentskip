package com.vertex.silentskip.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vertex.silentskip.presentation.ui.components.SilentSkipBottomBar
import com.vertex.silentskip.presentation.ui.recording.RecordingScreen
import com.vertex.silentskip.presentation.ui.recording.RecordingViewModel
import com.vertex.silentskip.presentation.ui.recordingslist.RecordingsListScreen
import com.vertex.silentskip.presentation.ui.recordingslist.RecordingsListViewModel
import com.vertex.silentskip.presentation.ui.settings.SettingsScreen
import com.vertex.silentskip.presentation.ui.settings.SettingsViewModel

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    recordingViewModel: RecordingViewModel = hiltViewModel(),
    recordingsListViewModel: RecordingsListViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val adState by recordingViewModel.adManagerAction.adState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            SilentSkipBottomBar(
                currentRoute = currentRoute,
                onNavigateToRoute = { route ->
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo(Screen.Recording.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Recording.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Recording.route) {
                RecordingScreen(
                    viewModel = recordingViewModel,
                    adState = adState
                )
            }
            composable(Screen.RecordingsList.route) {
                RecordingsListScreen(
                    viewModel = recordingsListViewModel,
                    adState = adState
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel
                )
            }
        }
    }
}
