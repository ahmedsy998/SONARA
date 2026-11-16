package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.ui.SonaraAppRoot
import com.example.viewmodel.SonaraViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SonaraViewModel by viewModels()
    private var incomingAudioUri by mutableStateOf<Uri?>(null)
    private var openPlayerOnLaunch by mutableStateOf(false)

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        parseIntent(intent)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            SonaraAppRoot(
                viewModel = viewModel,
                widthSizeClass = windowSizeClass.widthSizeClass,
                initialOpenPlayer = openPlayerOnLaunch,
                incomingAudioUri = incomingAudioUri
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        parseIntent(intent)
    }

    private fun parseIntent(intent: Intent?) {
        if (intent == null) return
        if (intent.getBooleanExtra("open_player", false)) {
            openPlayerOnLaunch = true
        }
        if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
            incomingAudioUri = intent.data
        }
    }
}
