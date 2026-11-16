package com.example.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.BackgroundTreatment
import com.example.model.CardStyle
import com.example.model.SonaraThemeSpec
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun SonaraThemeSpec.cardShape(extraRadiusDelta: Float = 0f): Shape {
    val r = (cornerRadiusDp + extraRadiusDelta).coerceAtLeast(0f).dp
    return when (cardStyle) {
        CardStyle.CYBER_CHAMFER_GLOW -> CutCornerShape(topStart = r, bottomEnd = r, topEnd = 3.dp, bottomStart = 3.dp)
        CardStyle.EDITORIAL_RULED -> RoundedCornerShape(2.dp)
        else -> RoundedCornerShape(r)
    }
}

@Composable
fun SonaraThemedCardSurface(
    spec: SonaraThemeSpec,
    modifier: Modifier = Modifier,
    elevated: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = spec.cardShape()
    val baseColor = (if (elevated) spec.surfaceElevated else spec.surface)
        .copy(alpha = spec.cardAlpha)

    val cardModifier = when (spec.cardStyle) {
        CardStyle.TRANSLUCENT_GLASS -> {
            modifier
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            baseColor.copy(alpha = (spec.cardAlpha * 0.95f).coerceAtLeast(0.28f)),
                            spec.primary.copy(alpha = 0.08f)
                        )
                    )
                )
                .border(
                    BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = if (spec.isDark) 0.35f else 0.7f),
                                spec.primary.copy(alpha = 0.3f)
                            )
                        )
                    ),
                    shape
                )
        }

        CardStyle.NOTHING_DOTTED_OUTLINE -> {
            val radiusPx = spec.cornerRadiusDp
            modifier
                .clip(shape)
                .background(baseColor)
                .drawBehind {
                    drawRoundRect(
                        color = spec.borderColor,
                        cornerRadius = CornerRadius(radiusPx.dp.toPx(), radiusPx.dp.toPx()),
                        style = Stroke(
                            width = 1.4.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )
                    )
                }
        }

        CardStyle.CYBER_CHAMFER_GLOW -> {
            modifier
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            spec.surface.copy(alpha = 0.92f),
                            spec.secondary.copy(alpha = 0.14f)
                        )
                    )
                )
                .border(
                    1.2.dp,
                    Brush.linearGradient(listOf(spec.primary, spec.secondary)),
                    shape
                )
        }

        CardStyle.NEUMORPHIC_SOFT -> {
            val lightShadow = if (spec.isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.9f)
            val darkShadow = if (spec.isDark) Color.Black.copy(alpha = 0.55f) else Color(0xFFB8C4D9).copy(alpha = 0.65f)
            modifier
                .clip(shape)
                .background(baseColor)
                .border(
                    1.5.dp,
                    Brush.linearGradient(listOf(lightShadow, darkShadow)),
                    shape
                )
        }

        CardStyle.HOLOGRAPHIC_IRIDESCENT -> {
            modifier
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            spec.surface.copy(alpha = spec.cardAlpha),
                            spec.primary.copy(alpha = 0.16f),
                            spec.secondary.copy(alpha = 0.16f)
                        )
                    )
                )
                .border(
                    1.3.dp,
                    Brush.sweepGradient(listOf(spec.primary, spec.secondary, spec.accent, spec.primary)),
                    shape
                )
        }

        CardStyle.LUXURY_GOLD_BORDER -> {
            modifier
                .clip(shape)
                .background(baseColor)
                .border(0.9.dp, spec.primary.copy(alpha = 0.55f), shape)
        }

        CardStyle.EDITORIAL_RULED -> {
            modifier
                .clip(shape)
                .background(baseColor)
                .border(1.dp, spec.borderColor, shape)
        }

        CardStyle.CASSETTE_PLASTIC_BEVEL -> {
            modifier
                .clip(shape)
                .background(baseColor)
                .border(2.dp, spec.borderColor, shape)
        }

        CardStyle.WALKMAN_BRUSHED_METAL -> {
            modifier
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            spec.surfaceElevated.copy(alpha = 0.98f),
                            spec.surface.copy(alpha = 0.98f)
                        )
                    )
                )
                .border(1.dp, spec.primary.copy(alpha = 0.35f), shape)
        }

        else -> {
            modifier
                .clip(shape)
                .background(baseColor)
                .border(0.8.dp, spec.borderColor.copy(alpha = 0.55f), shape)
        }
    }

    Box(modifier = cardModifier, content = content)
}

@Composable
fun SonaraThemedBackground(
    spec: SonaraThemeSpec,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val animateAurora = spec.backgroundTreatment == BackgroundTreatment.FLOWING_AURORA_CANVAS ||
        spec.backgroundTreatment == BackgroundTreatment.PRISM_IRIDESCENT_SWEEP

    val phase by if (animateAurora) {
        val transition = rememberInfiniteTransition(label = "bg_aurora")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = (2f * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 14000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "aurora_phase"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            if (spec.useGradientBackground) {
                val rad = Math.toRadians(spec.gradientAngleDeg.toDouble())
                val dx = cos(rad).toFloat() * w * 0.5f
                val dy = sin(rad).toFloat() * h * 0.5f
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(spec.gradientStart, spec.gradientEnd),
                        start = Offset(w * 0.5f - dx, h * 0.5f - dy),
                        end = Offset(w * 0.5f + dx, h * 0.5f + dy)
                    )
                )
            } else {
                drawRect(color = spec.background)
            }

            when (spec.backgroundTreatment) {
                BackgroundTreatment.RADIAL_GLOW_MESH -> {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(spec.primary.copy(alpha = 0.24f), Color.Transparent),
                            center = Offset(w * 0.15f, h * 0.18f),
                            radius = w * 0.7f
                        ),
                        center = Offset(w * 0.15f, h * 0.18f),
                        radius = w * 0.7f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(spec.secondary.copy(alpha = 0.22f), Color.Transparent),
                            center = Offset(w * 0.88f, h * 0.78f),
                            radius = w * 0.75f
                        ),
                        center = Offset(w * 0.88f, h * 0.78f),
                        radius = w * 0.75f
                    )
                }

                BackgroundTreatment.FLOWING_AURORA_CANVAS,
                BackgroundTreatment.PRISM_IRIDESCENT_SWEEP -> {
                    val x1 = w * (0.3f + 0.25f * cos(phase))
                    val y1 = h * (0.25f + 0.15f * sin(phase))
                    val x2 = w * (0.7f + 0.2f * sin(phase * 0.8f))
                    val y2 = h * (0.7f + 0.15f * cos(phase * 0.8f))
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(spec.primary.copy(alpha = 0.28f), Color.Transparent),
                            center = Offset(x1, y1),
                            radius = w * 0.8f
                        ),
                        center = Offset(x1, y1),
                        radius = w * 0.8f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(spec.accent.copy(alpha = 0.22f), Color.Transparent),
                            center = Offset(x2, y2),
                            radius = w * 0.75f
                        ),
                        center = Offset(x2, y2),
                        radius = w * 0.75f
                    )
                }

                BackgroundTreatment.DOT_MATRIX_GRID -> {
                    val step = 28.dp.toPx()
                    val dotColor = spec.textSecondary.copy(alpha = 0.16f)
                    var x = step * 0.5f
                    while (x < w) {
                        var y = step * 0.5f
                        while (y < h) {
                            drawCircle(color = dotColor, radius = 1.2.dp.toPx(), center = Offset(x, y))
                            y += step
                        }
                        x += step
                    }
                }

                BackgroundTreatment.CYBER_SCANLINE_GRID -> {
                    val gridStep = 36.dp.toPx()
                    val lineColor = spec.primary.copy(alpha = 0.08f)
                    var gx = 0f
                    while (gx < w) {
                        drawLine(lineColor, Offset(gx, 0f), Offset(gx, h), strokeWidth = 1f)
                        gx += gridStep
                    }
                    var gy = 0f
                    while (gy < h) {
                        drawLine(lineColor, Offset(0f, gy), Offset(w, gy), strokeWidth = 1f)
                        gy += gridStep
                    }
                }

                BackgroundTreatment.VINTAGE_TAPE_TEXTURE -> {
                    drawLine(
                        color = spec.primary.copy(alpha = 0.14f),
                        start = Offset(0f, h * 0.14f),
                        end = Offset(w, h * 0.14f),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = spec.secondary.copy(alpha = 0.14f),
                        start = Offset(0f, h * 0.155f),
                        end = Offset(w, h * 0.155f),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                BackgroundTreatment.EDITORIAL_PAPER_CONTRAST -> {
                    drawLine(
                        color = spec.borderColor.copy(alpha = 0.4f),
                        start = Offset(20.dp.toPx(), 0f),
                        end = Offset(20.dp.toPx(), h),
                        strokeWidth = 0.7.dp.toPx()
                    )
                }

                else -> Unit
            }
        }
        content()
    }
}

fun SonaraThemeSpec.horizontalScreenPadding(): Dp {
    return when (density) {
        com.example.model.UiDensity.COMPACT -> 12.dp
        com.example.model.UiDensity.COMFORTABLE -> 16.dp
        com.example.model.UiDensity.SPACIOUS -> 22.dp
    }
}

fun SonaraThemeSpec.verticalItemSpacing(): Dp {
    return when (density) {
        com.example.model.UiDensity.COMPACT -> 6.dp
        com.example.model.UiDensity.COMFORTABLE -> 10.dp
        com.example.model.UiDensity.SPACIOUS -> 14.dp
    }
}
