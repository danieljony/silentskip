package com.vertex.silentskip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vertex.silentskip.presentation.navigation.NavGraph
import com.vertex.silentskip.ui.theme.SilentSkipTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SilentSkipTheme {
                NavGraph()
            }
        }
    }
}
