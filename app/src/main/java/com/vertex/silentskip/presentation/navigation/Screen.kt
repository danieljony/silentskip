package com.vertex.silentskip.presentation.navigation

sealed class Screen(val route: String) {
    data object Recording : Screen("recording_screen")
    data object RecordingsList : Screen("recordings_list_screen")
    data object Settings : Screen("settings_screen")
}
