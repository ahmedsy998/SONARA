package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioTechnicalInfo
import com.example.model.AudioTrack
import com.example.model.LrcLine
import com.example.model.PlayerLayoutStyle
import com.example.model.RepeatModeState
import com.example.model.SonaraThemeId
import com.example.model.SonaraThemeSpec
import com.example.model.VisualizerStyle
import com.example.player.PlaybackUiState
import com.example.theme.SonaraThemedCardSurface
import com.example.theme.cardShape
import com.example.ui.components.SonaraAlbumArt
import com.example.ui.components.SonaraAudioVisualizerPanel
import com.example.ui.components.ThemeAwarePlayerArtwork
import com.example.ui.util.formatDuration
import com.example.ui.util.tr

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    playbackState: PlaybackUiState,
    techInfo: AudioTechnicalInfo,
    lyricsRaw: String?,
    lrcLines: List<LrcLine>,
    fftBands: FloatArray,
    waveform: FloatArray,
    visualizerStyle: VisualizerStyle,
    visualizerEnabled: Boolean,
    isLiveVisualizerActive: Boolean,
    hasAudioRecordPermission: Boolean,
    spec: SonaraThemeSpec,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onPlayQueueIndex: (Int) -> Unit,
    onMoveQueueItem: (Int, Int) -> Unit,
    onRemoveQueueItem: (Int) -> Unit,
    onSetSleepTimer: (Int) -> Unit,
    onRequestLiveVisualizer: () -> Unit,
    onCycleVisualizerStyle: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenTrackOptions: (AudioTrack) -> Unit,
    onEditLyrics: (AudioTrack) -> Unit
) {
    val track = playbackState.currentTrack
    val haptics = LocalHapticFeedback.current

    var fullScreenArtMode by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showLyricsSheet by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var dragAccumulatedX by remember { mutableFloatStateOf(0f) }

    BackHandler {
        when {
            fullScreenArtMode -> fullScreenArtMode = false
            showQueueSheet -> showQueueSheet = false
            showLyricsSheet -> showLyricsSheet = false
            else -> onDismiss()
        }
    }

    val bgBrush = Brush.verticalGradient(
        listOf(
            spec.primary.copy(alpha = if (spec.isDark) 0.24f else 0.14f),
            spec.background,
            spec.backgroundSecondary
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("now_playing_screen")
    ) {
        if (fullScreenArtMode && track != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { fullScreenArtMode = false }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                SonaraAlbumArt(
                    artUri = track.albumArtUri,
                    contentDescription = track.title,
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = spec.cornerRadiusDp.dp
                )
                IconButton(
                    onClick = { fullScreenArtMode = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.55f))
                ) {
                    Icon(Icons.Filled.FullscreenExit, contentDescription = "Exit full screen art", tint = Color.White)
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(track.title, color = spec.textPrimary, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                    Text(track.artist, color = spec.textSecondary, style = MaterialTheme.typography.titleMedium)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("player_collapse_btn")) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = tr(isArabic, "Collapse", "تصغير"), tint = spec.textPrimary, modifier = Modifier.size(30.dp))
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (spec.playerLayoutStyle) {
                                PlayerLayoutStyle.EDITORIAL_COVER_SPREAD -> tr(isArabic, "SONARA EDITION", "إصدار سونارا")
                                PlayerLayoutStyle.RETRO_CASSETTE_DECK -> "DECK A • HI-FI"
                                PlayerLayoutStyle.WALKMAN_JOG_WHEEL -> "WALKMAN ENGINE"
                                PlayerLayoutStyle.NOTHING_GLYPH_DIAL -> "GLYPH // AUDIO"
                                else -> tr(isArabic, "NOW PLAYING", "قيد التشغيل الآن")
                            },
                            color = spec.primary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (playbackState.sleepTimerRemainingSec > 0) {
                            val mins = playbackState.sleepTimerRemainingSec / 60
                            val secs = playbackState.sleepTimerRemainingSec % 60
                            Text(
                                text = "⏱ %d:%02d".format(mins, secs),
                                color = spec.accent,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = { fullScreenArtMode = true }) {
                            Icon(Icons.Filled.Fullscreen, contentDescription = "Full screen art", tint = spec.textPrimary)
                        }
                        if (track != null) {
                            IconButton(onClick = { onOpenTrackOptions(track) }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "Options", tint = spec.textPrimary)
                            }
                        }
                    }
                }

                // Artwork Stage with Swipe Left/Right gesture to skip tracks
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .padding(vertical = 8.dp)
                        .pointerInput(track?.id) {
                            detectHorizontalDragGestures(
                                onDragStart = { dragAccumulatedX = 0f },
                                onHorizontalDrag = { _, dragAmount -> dragAccumulatedX += dragAmount },
                                onDragEnd = {
                                    if (dragAccumulatedX < -120f) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onNext()
                                    } else if (dragAccumulatedX > 120f) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onPrevious()
                                    }
                                    dragAccumulatedX = 0f
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    ThemeAwarePlayerArtwork(
                        track = track,
                        isPlaying = playbackState.isPlaying,
                        spec = spec,
                        techInfo = techInfo,
                        modifier = Modifier
                            .fillMaxWidth(0.84f)
                            .clickable { fullScreenArtMode = true }
                    )
                }

                // Audio Visualizer Strip
                if (visualizerEnabled) {
                    SonaraAudioVisualizerPanel(
                        style = visualizerStyle,
                        fftBands = fftBands,
                        waveform = waveform,
                        isLiveCaptureActive = isLiveVisualizerActive,
                        hasAudioRecordPermission = hasAudioRecordPermission,
                        spec = spec,
                        isArabic = isArabic,
                        onRequestEnableLiveVisualizer = onRequestLiveVisualizer,
                        onCycleStyle = onCycleVisualizerStyle
                    )
                }

                // Track Info & Favorite
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track?.title ?: tr(isArabic, "No Track Selected", "لم يتم اختيار مقطع"),
                            color = spec.textPrimary,
                            style = if (spec.playerLayoutStyle == PlayerLayoutStyle.EDITORIAL_COVER_SPREAD) {
                                MaterialTheme.typography.headlineLarge
                            } else {
                                MaterialTheme.typography.headlineMedium
                            },
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track?.let { "${it.artist} — ${it.album}" } ?: "",
                            color = spec.textSecondary,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (track != null) {
                        IconButton(onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleFavorite(track.id)
                        }) {
                            Icon(
                                imageVector = if (track.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = tr(isArabic, "Favorite", "مفضلة"),
                                tint = if (track.isFavorite) spec.primary else spec.textSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                // Progress Scrubber
                val dur = playbackState.durationMs.coerceAtLeast(1L)
                var scrubbing by remember { mutableStateOf(false) }
                var scrubValue by remember { mutableFloatStateOf(0f) }
                val sliderPos = if (scrubbing) scrubValue else (playbackState.positionMs.toFloat() / dur).coerceIn(0f, 1f)

                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = sliderPos,
                        onValueChange = {
                            scrubbing = true
                            scrubValue = it
                        },
                        onValueChangeFinished = {
                            onSeek((scrubValue * dur).toLong())
                            scrubbing = false
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = spec.primary,
                            activeTrackColor = spec.primary,
                            inactiveTrackColor = spec.borderColor.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.testTag("player_seek_slider")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatDuration(if (scrubbing) (scrubValue * dur).toLong() else playbackState.positionMs),
                            color = spec.textSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                        val remaining = (dur - playbackState.positionMs).coerceAtLeast(0L)
                        Text(
                            text = "-${formatDuration(remaining)}",
                            color = spec.textSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Theme-Specific Transport Controls Deck
                SonaraThemedCardSurface(
                    spec = spec,
                    elevated = true,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onToggleShuffle, modifier = Modifier.testTag("player_shuffle_btn")) {
                            Icon(
                                imageVector = Icons.Filled.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (playbackState.shuffleEnabled) spec.primary else spec.textSecondary
                            )
                        }

                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onPrevious()
                            },
                            modifier = Modifier.testTag("player_prev_btn")
                        ) {
                            Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", tint = spec.textPrimary, modifier = Modifier.size(34.dp))
                        }

                        val playShape = when (spec.playerLayoutStyle) {
                            PlayerLayoutStyle.CYBERPUNK_HUD_DECK -> CutCornerShape(14.dp)
                            PlayerLayoutStyle.RETRO_CASSETTE_DECK,
                            PlayerLayoutStyle.FLUENT_STUDIO_PANEL -> RoundedCornerShape(12.dp)
                            else -> CircleShape
                        }
                        Box(
                            modifier = Modifier
                                .size(66.dp)
                                .clip(playShape)
                                .background(
                                    Brush.linearGradient(listOf(spec.primary, spec.secondary))
                                )
                                .clickable {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onPlayPause()
                                }
                                .testTag("player_play_pause_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (playbackState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Play or Pause",
                                tint = Color.Black,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onNext()
                            },
                            modifier = Modifier.testTag("player_next_btn")
                        ) {
                            Icon(Icons.Filled.SkipNext, contentDescription = "Next", tint = spec.textPrimary, modifier = Modifier.size(34.dp))
                        }

                        IconButton(onClick = onCycleRepeat, modifier = Modifier.testTag("player_repeat_btn")) {
                            Icon(
                                imageVector = if (playbackState.repeatMode == RepeatModeState.ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                                contentDescription = "Repeat",
                                tint = if (playbackState.repeatMode != RepeatModeState.OFF) spec.primary else spec.textSecondary
                            )
                        }
                    }
                }

                // Bottom Action Strip: Lyrics | Equalizer | Sleep Timer | Queue
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { showLyricsSheet = true }) {
                        Icon(Icons.Filled.Lyrics, contentDescription = null, tint = spec.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(tr(isArabic, "Lyrics", "الكلمات"), color = spec.textPrimary)
                    }
                    TextButton(onClick = onOpenEqualizer) {
                        Icon(Icons.Filled.Equalizer, contentDescription = null, tint = spec.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(tr(isArabic, "EQ", "المعادل"), color = spec.textPrimary)
                    }
                    TextButton(onClick = { showSleepTimerDialog = true }) {
                        Icon(Icons.Filled.Timer, contentDescription = null, tint = spec.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(tr(isArabic, "Timer", "المؤقت"), color = spec.textPrimary)
                    }
                    TextButton(onClick = { showQueueSheet = true }) {
                        Icon(Icons.Filled.QueueMusic, contentDescription = null, tint = spec.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(tr(isArabic, "Queue", "الطابور"), color = spec.textPrimary)
                    }
                }
            }
        }
    }

    // Queue Sheet with reordering
    if (showQueueSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQueueSheet = false },
            containerColor = spec.surface
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "${tr(isArabic, "Playback Queue", "قائمة الانتظار")} (${playbackState.queue.size})",
                    color = spec.textPrimary,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(10.dp))
                LazyColumn(modifier = Modifier.height(380.dp)) {
                    itemsIndexed(playbackState.queue, key = { idx, item -> "${item.id}_$idx" }) { index, qTrack ->
                        val isCurrent = index == playbackState.currentIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isCurrent) spec.primary.copy(alpha = 0.16f) else Color.Transparent)
                                .clickable { onPlayQueueIndex(index) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = if (isCurrent) spec.primary else spec.textSecondary,
                                modifier = Modifier.width(26.dp),
                                fontWeight = FontWeight.Bold
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = qTrack.title,
                                    color = if (isCurrent) spec.primary else spec.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(qTrack.artist, color = spec.textSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                            }
                            IconButton(onClick = { if (index > 0) onMoveQueueItem(index, index - 1) }, enabled = index > 0) {
                                Icon(Icons.Filled.ArrowUpward, contentDescription = "Move up", tint = spec.textSecondary)
                            }
                            IconButton(onClick = { if (index < playbackState.queue.lastIndex) onMoveQueueItem(index, index + 1) }, enabled = index < playbackState.queue.lastIndex) {
                                Icon(Icons.Filled.ArrowDownward, contentDescription = "Move down", tint = spec.textSecondary)
                            }
                            IconButton(onClick = { onRemoveQueueItem(index) }) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove", tint = spec.textSecondary)
                            }
                        }
                    }
                }
            }
        }
    }

    // Synchronized LRC Lyrics Sheet
    if (showLyricsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLyricsSheet = false },
            containerColor = spec.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(tr(isArabic, "Lyrics", "كلمات الأغنية"), color = spec.textPrimary, style = MaterialTheme.typography.titleLarge)
                    if (track != null) {
                        IconButton(onClick = {
                            showLyricsSheet = false
                            onEditLyrics(track)
                        }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit lyrics", tint = spec.primary)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))

                if (lrcLines.isNotEmpty()) {
                    val activeIndex = lrcLines.indexOfLast { it.timestampMs <= playbackState.positionMs }.coerceAtLeast(0)
                    val listState = rememberLazyListState()
                    LaunchedEffect(activeIndex) {
                        listState.animateScrollToItem((activeIndex - 2).coerceAtLeast(0))
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.height(340.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(lrcLines) { idx, line ->
                            val isActive = idx == activeIndex
                            Text(
                                text = line.text,
                                color = if (isActive) spec.primary else spec.textSecondary,
                                fontSize = if (isActive) 20.sp else 16.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSeek(line.timestampMs) }
                            )
                        }
                    }
                } else if (!lyricsRaw.isNullOrBlank()) {
                    LazyColumn(modifier = Modifier.height(340.dp)) {
                        item {
                            Text(
                                text = lyricsRaw,
                                color = spec.textPrimary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = tr(
                                isArabic,
                                "No lyrics found (.lrc file or custom lyrics).",
                                "لم يتم العثور على كلمات (.lrc أو كلمات مخصصة)."
                            ),
                            color = spec.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        if (track != null) {
                            Button(onClick = {
                                showLyricsSheet = false
                                onEditLyrics(track)
                            }) {
                                Text(tr(isArabic, "Add Lyrics", "إضافة كلمات"))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }

    // Sleep Timer Dialog
    if (showSleepTimerDialog) {
        val options = listOf(0, 10, 15, 30, 45, 60, 90)
        AlertDialog(
            onDismissRequest = { showSleepTimerDialog = false },
            title = { Text(tr(isArabic, "Sleep Timer", "مؤقت النوم")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    options.forEach { mins ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onSetSleepTimer(mins)
                                    showSleepTimerDialog = false
                                }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = if (mins == 0) tr(isArabic, "Turn Off", "إيقاف المؤقت") else "$mins ${tr(isArabic, "minutes", "دقيقة")}",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSleepTimerDialog = false }) {
                    Text(tr(isArabic, "Close", "إغلاق"))
                }
            }
        )
    }
}
