package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.ArtworkShapeStyle
import com.example.model.AudioTechnicalInfo
import com.example.model.AudioTrack
import com.example.model.SonaraThemeId
import com.example.model.SonaraThemeSpec
import com.example.ui.util.tr
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SonaraAlbumArt(
    artUri: Uri?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp
) {
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(artUri ?: R.drawable.img_default_album_art)
            .crossfade(true)
            .build(),
        placeholder = painterResource(R.drawable.img_default_album_art),
        error = painterResource(R.drawable.img_default_album_art),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(cornerRadius))
    )
}

@Composable
fun ThemeAwarePlayerArtwork(
    track: AudioTrack?,
    isPlaying: Boolean,
    spec: SonaraThemeSpec,
    techInfo: AudioTechnicalInfo,
    isArabic: Boolean = false,
    modifier: Modifier = Modifier
) {
    val spinTransition = rememberInfiniteTransition(label = "art_spin")
    val spinAngle by spinTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_deg"
    )
    val effectiveAngle = if (isPlaying) spinAngle else 0f
    val defaultArtDesc = tr(isArabic, "Album Art", "غلاف الألبوم")

    when (spec.artworkShapeStyle) {
        ArtworkShapeStyle.CASSETTE_SHELL -> {
            RetroCassetteDeckArtwork(
                track = track,
                isPlaying = isPlaying,
                spinAngle = effectiveAngle,
                spec = spec,
                isArabic = isArabic,
                modifier = modifier
            )
        }

        ArtworkShapeStyle.CIRCLE_VINYL -> {
            Box(
                modifier = modifier.aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                // Ambient vinyl halo
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(spec.primary.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.86f)
                        .rotate(effectiveAngle)
                        .clip(CircleShape)
                        .border(3.dp, spec.primary.copy(alpha = 0.7f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    SonaraAlbumArt(
                        artUri = track?.albumArtUri,
                        contentDescription = track?.title ?: defaultArtDesc,
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = 300.dp
                    )
                    // Vinyl center spindle
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(spec.background)
                            .border(2.dp, spec.primary, CircleShape)
                    )
                }
            }
        }

        ArtworkShapeStyle.CHAMFERED_HEX -> {
            val chamfer = CutCornerShape(24.dp)
            Box(
                modifier = modifier
                    .aspectRatio(1f)
                    .clip(chamfer)
                    .border(
                        2.dp,
                        Brush.linearGradient(listOf(spec.primary, spec.secondary)),
                        chamfer
                    )
                    .padding(4.dp)
            ) {
                SonaraAlbumArt(
                    artUri = track?.albumArtUri,
                    contentDescription = track?.title ?: defaultArtDesc,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(chamfer),
                    cornerRadius = 0.dp
                )
            }
        }

        ArtworkShapeStyle.SCALLOPED_FLOWER -> {
            val scallopedShape = GenericShape { size, _ ->
                val cx = size.width / 2f
                val cy = size.height / 2f
                val baseR = minOf(cx, cy) * 0.90f
                val amp = minOf(cx, cy) * 0.08f
                val lobes = 10
                val steps = 160
                for (i in 0..steps) {
                    val theta = (i.toFloat() / steps) * (2f * PI.toFloat())
                    val r = baseR + amp * cos(lobes * theta)
                    val x = cx + r * cos(theta)
                    val y = cy + r * sin(theta)
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            Box(
                modifier = modifier
                    .aspectRatio(1f)
                    .clip(scallopedShape)
                    .border(2.dp, spec.primary.copy(alpha = 0.5f), scallopedShape)
            ) {
                SonaraAlbumArt(
                    artUri = track?.albumArtUri,
                    contentDescription = track?.title ?: defaultArtDesc,
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 0.dp
                )
            }
        }

        ArtworkShapeStyle.TILTED_HOLO_CARD -> {
            val holoShape = RoundedCornerShape(spec.cornerRadiusDp.dp)
            Box(
                modifier = modifier
                    .aspectRatio(1f)
                    .graphicsLayer {
                        rotationY = if (isPlaying) 6f * sin(effectiveAngle * PI.toFloat() / 180f) else 4f
                        cameraDistance = 12f * density
                    }
                    .shadow(16.dp, holoShape, ambientColor = spec.primary, spotColor = spec.secondary)
                    .clip(holoShape)
                    .border(
                        2.dp,
                        Brush.sweepGradient(listOf(spec.primary, spec.secondary, spec.accent, spec.primary)),
                        holoShape
                    )
            ) {
                SonaraAlbumArt(
                    artUri = track?.albumArtUri,
                    contentDescription = track?.title ?: defaultArtDesc,
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = spec.cornerRadiusDp.dp
                )
            }
        }

        ArtworkShapeStyle.SHARP_EDITORIAL -> {
            Box(
                modifier = modifier
                    .aspectRatio(1f)
                    .border(1.5.dp, spec.textPrimary, RoundedCornerShape(2.dp))
                    .padding(6.dp)
            ) {
                SonaraAlbumArt(
                    artUri = track?.albumArtUri,
                    contentDescription = track?.title ?: defaultArtDesc,
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 2.dp
                )
            }
        }

        else -> {
            val r = spec.cornerRadiusDp.dp
            Box(
                modifier = modifier
                    .aspectRatio(1f)
                    .shadow(
                        elevation = if (spec.id == SonaraThemeId.AMOLED_BLACK) 0.dp else 18.dp,
                        shape = RoundedCornerShape(r),
                        ambientColor = spec.primary.copy(alpha = 0.4f),
                        spotColor = spec.primary.copy(alpha = 0.4f)
                    )
                    .clip(RoundedCornerShape(r))
                    .border(1.dp, spec.borderColor.copy(alpha = 0.6f), RoundedCornerShape(r))
            ) {
                SonaraAlbumArt(
                    artUri = track?.albumArtUri,
                    contentDescription = track?.title ?: defaultArtDesc,
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = r
                )
                if (spec.id == SonaraThemeId.SONY_WALKMAN && techInfo.formatLabel.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.78f))
                            .border(1.dp, spec.primary, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        val specTxt = buildString {
                            append(techInfo.formatLabel)
                            if (techInfo.bitrateKbps > 0) append(" • ${techInfo.bitrateKbps}kbps")
                        }
                        Text(
                            text = specTxt,
                            color = spec.primary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RetroCassetteDeckArtwork(
    track: AudioTrack?,
    isPlaying: Boolean,
    spinAngle: Float,
    spec: SonaraThemeSpec,
    isArabic: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(spec.surfaceElevated, spec.surface)
                )
            )
            .border(2.5.dp, spec.borderColor, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Cassette top label strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(spec.primary.copy(alpha = 0.18f))
                    .border(1.dp, spec.primary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${tr(isArabic, "SIDE A", "الوجه أ")} • ${track?.title ?: tr(isArabic, "SONARA TAPE", "شريط سونارا")}",
                    color = spec.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (isPlaying) tr(isArabic, "REC/PLAY", "تشغيل") else tr(isArabic, "STBY", "استعداد"),
                    color = spec.accent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            // Tape window with dual spinning spools + thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF12100E))
                    .border(1.5.dp, spec.borderColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cy = size.height / 2f
                    val leftX = size.width * 0.22f
                    val rightX = size.width * 0.78f
                    val rOuter = 30.dp.toPx()
                    val rInner = 14.dp.toPx()

                    // Magnetic tape ribbon
                    drawLine(
                        color = Color(0xFF5C4033),
                        start = Offset(leftX, cy + rOuter * 0.65f),
                        end = Offset(rightX, cy + rOuter * 0.65f),
                        strokeWidth = 3.dp.toPx()
                    )

                    listOf(leftX, rightX).forEach { cx ->
                        drawCircle(
                            color = Color(0xFF2B2623),
                            radius = rOuter,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = spec.primary,
                            radius = rInner,
                            center = Offset(cx, cy),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        rotate(spinAngle, pivot = Offset(cx, cy)) {
                            for (spoke in 0 until 6) {
                                val a = spoke * 60f * (PI.toFloat() / 180f)
                                drawLine(
                                    color = Color(0xFFEDE4D3),
                                    start = Offset(cx + cos(a) * 5.dp.toPx(), cy + sin(a) * 5.dp.toPx()),
                                    end = Offset(cx + cos(a) * (rInner - 2.dp.toPx()), cy + sin(a) * (rInner - 2.dp.toPx())),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                        }
                    }
                }

                SonaraAlbumArt(
                    artUri = track?.albumArtUri,
                    contentDescription = track?.title ?: tr(isArabic, "Cassette Art", "غلاف الكاسيت"),
                    modifier = Modifier
                        .size(56.dp)
                        .border(1.5.dp, spec.primary, RoundedCornerShape(8.dp)),
                    cornerRadius = 8.dp
                )
            }

            // Bottom trapezoid mechanical deck guide
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TYPE II • CrO2 70μs",
                    color = spec.textSecondary,
                    fontSize = 10.sp
                )
                Text(
                    text = track?.artist ?: tr(isArabic, "ANALOG ENGINE", "محرك تناظري"),
                    color = spec.primary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
