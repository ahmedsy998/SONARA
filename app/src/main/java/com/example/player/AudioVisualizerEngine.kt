package com.example.player

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.audiofx.Visualizer
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.hypot
import kotlin.math.log10

class AudioVisualizerEngine(private val context: Context) {

    companion object {
        const val BAND_COUNT = 28
    }

    private var visualizer: Visualizer? = null
    private var currentSessionId: Int = 0

    private val _fftBands = MutableStateFlow(FloatArray(BAND_COUNT) { 0f })
    val fftBands: StateFlow<FloatArray> = _fftBands.asStateFlow()

    private val _waveform = MutableStateFlow(FloatArray(64) { 0f })
    val waveform: StateFlow<FloatArray> = _waveform.asStateFlow()

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun start(audioSessionId: Int, enabled: Boolean) {
        if (!enabled || audioSessionId <= 0 || !hasAudioPermission()) {
            stop()
            return
        }
        if (visualizer != null && currentSessionId == audioSessionId) {
            runCatching { visualizer?.enabled = true }
            _isActive.value = true
            return
        }

        stop()
        currentSessionId = audioSessionId

        runCatching {
            val captureSize = Visualizer.getCaptureSizeRange()[1].coerceAtMost(1024)
            val rate = (Visualizer.getMaxCaptureRate() / 2).coerceAtLeast(10000)
            visualizer = Visualizer(audioSessionId).apply {
                this.captureSize = captureSize
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            v: Visualizer?,
                            waveformBytes: ByteArray?,
                            samplingRate: Int
                        ) {
                            waveformBytes ?: return
                            val step = (waveformBytes.size / 64).coerceAtLeast(1)
                            val normalized = FloatArray(64) { i ->
                                val idx = (i * step).coerceAtMost(waveformBytes.lastIndex)
                                ((waveformBytes[idx].toInt() and 0xFF) - 128) / 128f
                            }
                            _waveform.value = normalized
                        }

                        override fun onFftDataCapture(
                            v: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            fft ?: return
                            val magnitudes = FloatArray(BAND_COUNT)
                            val n = fft.size / 2
                            val binsPerBand = (n / BAND_COUNT).coerceAtLeast(1)
                            for (b in 0 until BAND_COUNT) {
                                var sum = 0f
                                val startIdx = 1 + b * binsPerBand
                                val endIdx = (startIdx + binsPerBand).coerceAtMost(n)
                                for (k in startIdx until endIdx) {
                                    val re = fft[2 * k].toFloat()
                                    val im = fft[2 * k + 1].toFloat()
                                    val mag = hypot(re, im)
                                    sum += mag
                                }
                                val avg = if (endIdx > startIdx) sum / (endIdx - startIdx) else 0f
                                val db = if (avg > 1f) (20f * log10(avg)) / 50f else 0f
                                magnitudes[b] = db.coerceIn(0.03f, 1f)
                            }
                            _fftBands.value = magnitudes
                        }
                    },
                    rate,
                    true,
                    true
                )
                this.enabled = true
            }
            _isActive.value = true
        }.onFailure {
            stop()
        }
    }

    fun stop() {
        runCatching {
            visualizer?.enabled = false
            visualizer?.release()
        }
        visualizer = null
        currentSessionId = 0
        _isActive.value = false
        _fftBands.value = FloatArray(BAND_COUNT) { 0f }
        _waveform.value = FloatArray(64) { 0f }
    }
}
