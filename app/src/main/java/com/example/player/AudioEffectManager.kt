package com.example.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import com.example.model.EqualizerConfig
import kotlin.math.roundToInt

class AudioEffectManager {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    private var currentSessionId: Int = 0
    private var lastConfig: EqualizerConfig = EqualizerConfig()

    var hardwareBandCount: Int = 5
        private set
    var isEqualizerSupported: Boolean = true
        private set
    var isBassBoostSupported: Boolean = true
        private set
    var isVirtualizerSupported: Boolean = true
        private set

    fun attachToSession(audioSessionId: Int, config: EqualizerConfig = lastConfig) {
        if (audioSessionId <= 0) return
        if (audioSessionId == currentSessionId && equalizer != null) {
            applyConfig(config)
            return
        }

        release()
        currentSessionId = audioSessionId
        lastConfig = config

        runCatching {
            equalizer = Equalizer(0, audioSessionId).apply {
                hardwareBandCount = numberOfBands.toInt().coerceAtLeast(1)
            }
            isEqualizerSupported = true
        }.onFailure {
            isEqualizerSupported = false
        }

        runCatching {
            bassBoost = BassBoost(0, audioSessionId).apply {
                isBassBoostSupported = strengthSupported
            }
        }.onFailure {
            isBassBoostSupported = false
        }

        runCatching {
            virtualizer = Virtualizer(0, audioSessionId).apply {
                isVirtualizerSupported = strengthSupported
            }
        }.onFailure {
            isVirtualizerSupported = false
        }

        applyConfig(config)
    }

    fun applyConfig(config: EqualizerConfig) {
        lastConfig = config

        equalizer?.let { eq ->
            runCatching {
                eq.enabled = config.enabled
                val range = eq.bandLevelRange
                val minLevel = range[0].toInt()
                val maxLevel = range[1].toInt()
                val numHwBands = eq.numberOfBands.toInt().coerceAtLeast(1)
                val uiBands = config.bandLevelsDb

                for (hwBand in 0 until numHwBands) {
                    // Map 10-band UI slider values (-12..+12 dB) proportionally to hardware bands
                    val mappedIdx = if (numHwBands == 1) 0 else {
                        ((hwBand.toFloat() / (numHwBands - 1)) * (uiBands.size - 1)).roundToInt()
                    }.coerceIn(0, uiBands.lastIndex)

                    val db = uiBands[mappedIdx].coerceIn(-12, 12)
                    val millibels = (db * 100).coerceIn(minLevel, maxLevel).toShort()
                    eq.setBandLevel(hwBand.toShort(), millibels)
                }
            }
        }

        bassBoost?.let { bb ->
            runCatching {
                bb.enabled = config.enabled && config.bassBoostStrength > 0
                if (bb.strengthSupported) {
                    bb.setStrength(config.bassBoostStrength.coerceIn(0, 1000).toShort())
                }
            }
        }

        virtualizer?.let { virt ->
            runCatching {
                virt.enabled = config.enabled && config.virtualizerStrength > 0
                if (virt.strengthSupported) {
                    virt.setStrength(config.virtualizerStrength.coerceIn(0, 1000).toShort())
                }
            }
        }
    }

    fun release() {
        runCatching { equalizer?.release() }
        runCatching { bassBoost?.release() }
        runCatching { virtualizer?.release() }
        equalizer = null
        bassBoost = null
        virtualizer = null
        currentSessionId = 0
    }
}
