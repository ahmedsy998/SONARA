package com.example.player

import android.content.Context

object SonaraAudioBridge {
    val effectManager = AudioEffectManager()

    @Volatile
    private var visualizerEngine: AudioVisualizerEngine? = null

    fun getVisualizerEngine(context: Context): AudioVisualizerEngine {
        return visualizerEngine ?: synchronized(this) {
            visualizerEngine ?: AudioVisualizerEngine(context.applicationContext).also {
                visualizerEngine = it
            }
        }
    }

    @Volatile
    var currentAudioSessionId: Int = 0
}
