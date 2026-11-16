package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.SonaraThemeSpec
import com.example.model.VisualizerStyle
import com.example.ui.util.tr
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SonaraAudioVisualizerPanel(
    style: VisualizerStyle,
    fftBands: FloatArray,
    waveform: FloatArray,
    isLiveCaptureActive: Boolean,
    hasAudioRecordPermission: Boolean,
    spec: SonaraThemeSpec,
    isArabic: Boolean,
    onRequestEnableLiveVisualizer: () -> Unit,
    onCycleStyle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onCycleStyle)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("audio_visualizer_canvas")
            ) {
                val w = size.width
                val h = size.height
                val midY = h / 2f

                when (style) {
                    VisualizerStyle.SPECTRUM_BARS -> {
                        val count = fftBands.size.coerceAtLeast(1)
                        val slot = w / count
                        val barW = (slot * 0.58f).coerceAtLeast(3.dp.toPx())
                        for (i in 0 until count) {
                            val amp = fftBands[i].coerceIn(0.03f, 1f)
                            val barH = (amp * h * 0.88f).coerceAtLeast(3.dp.toPx())
                            drawRoundRect(
                                brush = Brush.verticalGradient(listOf(spec.accent, spec.primary)),
                                topLeft = Offset(i * slot + (slot - barW) / 2f, h - barH),
                                size = Size(barW, barH),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        }
                    }

                    VisualizerStyle.CIRCULAR_WAVES -> {
                        val cx = w / 2f
                        val cy = h / 2f
                        val baseR = (minOf(w, h) * 0.28f)
                        val count = fftBands.size.coerceAtLeast(1)
                        for (i in 0 until count) {
                            val theta = (i.toFloat() / count) * (2f * PI.toFloat())
                            val amp = fftBands[i].coerceIn(0.02f, 1f)
                            val len = amp * minOf(w, h) * 0.22f
                            val sx = cx + cos(theta) * baseR
                            val sy = cy + sin(theta) * baseR
                            val ex = cx + cos(theta) * (baseR + len)
                            val ey = cy + sin(theta) * (baseR + len)
                            drawLine(
                                color = if (i % 2 == 0) spec.primary else spec.secondary,
                                start = Offset(sx, sy),
                                end = Offset(ex, ey),
                                strokeWidth = 2.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    VisualizerStyle.MINIMAL_WAVEFORM -> {
                        val path = Path()
                        val count = waveform.size.coerceAtLeast(2)
                        val step = w / (count - 1)
                        for (i in 0 until count) {
                            val x = i * step
                            val y = midY + waveform[i].coerceIn(-1f, 1f) * (h * 0.42f)
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(
                            path = path,
                            brush = Brush.horizontalGradient(listOf(spec.primary, spec.accent, spec.secondary)),
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    VisualizerStyle.NEON_PARTICLES -> {
                        val count = fftBands.size.coerceAtLeast(1)
                        val step = w / count
                        for (i in 0 until count) {
                            val amp = fftBands[i].coerceIn(0.02f, 1f)
                            val x = i * step + step / 2f
                            val yTop = midY - amp * (h * 0.40f)
                            val yBot = midY + amp * (h * 0.40f)
                            val r = (2.5f + amp * 5f).dp.toPx()
                            drawCircle(
                                color = spec.primary.copy(alpha = 0.85f),
                                radius = r,
                                center = Offset(x, yTop)
                            )
                            drawCircle(
                                color = spec.secondary.copy(alpha = 0.65f),
                                radius = r * 0.75f,
                                center = Offset(x, yBot)
                            )
                        }
                    }

                    VisualizerStyle.REACTIVE_ABSTRACT -> {
                        val pathTop = Path()
                        val pathBot = Path()
                        val count = fftBands.size.coerceAtLeast(2)
                        val step = w / (count - 1)
                        pathTop.moveTo(0f, midY)
                        pathBot.moveTo(0f, midY)
                        for (i in 0 until count) {
                            val x = i * step
                            val amp = fftBands[i].coerceIn(0.02f, 1f) * (h * 0.44f)
                            pathTop.lineTo(x, midY - amp)
                            pathBot.lineTo(x, midY + amp)
                        }
                        pathTop.lineTo(w, midY)
                        pathBot.lineTo(w, midY)
                        drawPath(pathTop, brush = Brush.horizontalGradient(listOf(spec.primary, spec.accent)), style = Stroke(2.dp.toPx()))
                        drawPath(pathBot, brush = Brush.horizontalGradient(listOf(spec.secondary, spec.primary)), style = Stroke(2.dp.toPx()))
                    }
                }
            }

            if (!isLiveCaptureActive) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onRequestEnableLiveVisualizer)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = spec.primary
                    )
                    Text(
                        text = if (hasAudioRecordPermission) {
                            tr(isArabic, "Tap to start Live Audio FFT", "اضغط لتفعيل محلل الصوت الحي")
                        } else {
                            tr(isArabic, "Enable Live Audio Visualizer", "تفعيل المرئيات الصوتية الحية")
                        },
                        color = spec.textSecondary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        Text(
            text = "${tr(isArabic, style.labelEn, style.labelAr)} • ${tr(isArabic, "Tap to switch mode", "اضغط لتغيير النمط")}",
            color = spec.textSecondary.copy(alpha = 0.78f),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
