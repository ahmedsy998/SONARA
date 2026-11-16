package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AudioTrack
import com.example.model.SonaraThemeId
import com.example.model.SonaraThemeSpec
import com.example.theme.SonaraThemeCatalog
import com.example.theme.SonaraThemedCardSurface
import com.example.theme.cardShape
import com.example.theme.horizontalScreenPadding
import com.example.theme.verticalItemSpacing
import com.example.ui.components.SonaraAlbumArt
import com.example.ui.components.TrackRowItem
import com.example.ui.util.tr

@Composable
fun HomeScreen(
    allTracks: List<AudioTrack>,
    recentlyPlayed: List<AudioTrack>,
    mostPlayed: List<AudioTrack>,
    favorites: List<AudioTrack>,
    recentlyAdded: List<AudioTrack>,
    currentTrackId: Long?,
    hasAudioPermission: Boolean,
    isScanning: Boolean,
    spec: SonaraThemeSpec,
    isArabic: Boolean,
    onRequestAudioPermission: () -> Unit,
    onImportAudioFile: () -> Unit,
    onRescanLibrary: () -> Unit,
    onShuffleAll: () -> Unit,
    onPlayTrack: (AudioTrack, List<AudioTrack>) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onTrackOptions: (AudioTrack) -> Unit,
    onSelectTheme: (SonaraThemeId) -> Unit,
    onOpenThemeStudio: () -> Unit
) {
    val hPad = spec.horizontalScreenPadding()
    val vSpace = spec.verticalItemSpacing()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(start = hPad, end = hPad, top = 12.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(vSpace + 6.dp)
    ) {
        // Header tailored to theme architecture (One UI large header vs Editorial masthead vs compact)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (spec.id == SonaraThemeId.SAMSUNG_ONE_UI) {
                    Spacer(Modifier.height(14.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (spec.id == SonaraThemeId.EDITORIAL_MUSIC) {
                                tr(isArabic, "ISSUE NO. 01 • SONARA", "العدد 01 • سونارا")
                            } else {
                                "SONARA"
                            },
                            color = spec.primary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tr(isArabic, "Your Soundstage", "مسرح الصوت الخاص بك"),
                            color = spec.textPrimary,
                            style = if (spec.id == SonaraThemeId.SAMSUNG_ONE_UI || spec.id == SonaraThemeId.EDITORIAL_MUSIC) {
                                MaterialTheme.typography.displayLarge
                            } else {
                                MaterialTheme.typography.headlineLarge
                            }
                        )
                    }
                    Row {
                        IconButton(onClick = onRescanLibrary, modifier = Modifier.testTag("rescan_button")) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = tr(isArabic, "Rescan Library", "إعادة فحص المكتبة"),
                                tint = spec.textPrimary
                            )
                        }
                        IconButton(onClick = onOpenThemeStudio, modifier = Modifier.testTag("open_theme_studio_btn")) {
                            Icon(
                                imageVector = Icons.Filled.Palette,
                                contentDescription = tr(isArabic, "Theme Studio", "استوديو الثيمات"),
                                tint = spec.primary
                            )
                        }
                    }
                }
            }
        }

        // Hero Banner Card
        item {
            SonaraThemedCardSurface(
                spec = spec,
                elevated = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_sonara_hero),
                    contentDescription = "SONARA Soundstage",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alpha = if (spec.isDark) 0.52f else 0.35f
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    spec.background.copy(alpha = 0.85f),
                                    spec.background.copy(alpha = 0.25f)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.CenterStart),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${allTracks.size} ${tr(isArabic, "Local Tracks Indexed", "مقطع محلي مفهرس")}",
                            color = spec.primary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tr(
                                isArabic,
                                "Active Theme: ${spec.id.displayNameEn}",
                                "الثيم النشط: ${spec.id.displayNameAr}"
                            ),
                            color = spec.textPrimary,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    if (allTracks.isNotEmpty()) {
                                        onPlayTrack(allTracks.first(), allTracks)
                                    } else if (!hasAudioPermission) {
                                        onRequestAudioPermission()
                                    } else {
                                        onImportAudioFile()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = spec.primary),
                                modifier = Modifier.testTag("home_play_all_btn")
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.Black)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (allTracks.isNotEmpty()) {
                                        tr(isArabic, "Play All", "تشغيل الكل")
                                    } else {
                                        tr(isArabic, "Scan Music", "فحص الموسيقى")
                                    },
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    if (allTracks.isNotEmpty()) onShuffleAll() else onImportAudioFile()
                                },
                                modifier = Modifier.testTag("home_shuffle_btn")
                            ) {
                                Icon(
                                    imageVector = if (allTracks.isNotEmpty()) Icons.Filled.Shuffle else Icons.Filled.AudioFile,
                                    contentDescription = null,
                                    tint = spec.textPrimary
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (allTracks.isNotEmpty()) {
                                        tr(isArabic, "Shuffle", "عشوائي")
                                    } else {
                                        tr(isArabic, "Open File", "فتح ملف")
                                    },
                                    color = spec.textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Permission or Empty Library Helper Card
        if (!hasAudioPermission || (allTracks.isEmpty() && !isScanning)) {
            item {
                SonaraThemedCardSurface(spec = spec, modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (!hasAudioPermission) {
                                tr(isArabic, "Grant Audio Library Access", "امنح صلاحية الوصول للموسيقى")
                            } else {
                                tr(isArabic, "No Local Audio Files Found", "لم يتم العثور على ملفات صوتية")
                            },
                            color = spec.textPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (!hasAudioPermission) {
                                tr(
                                    isArabic,
                                    "Allow SONARA to scan MP3, FLAC, WAV, AAC, M4A, OGG, and OPUS files stored on your device, or pick a file directly.",
                                    "اسمح لـ SONARA بفحص ملفات الصوت المخزنة على جهازك أو افتح ملفاً صوتياً مباشرة."
                                )
                            } else {
                                tr(
                                    isArabic,
                                    "Add audio files to your device's Music/Download folders and tap Rescan, or open any audio file via the file picker.",
                                    "أضف ملفات صوتية إلى مجلد Music واضغط إعادة الفحص، أو افتح ملفاً مباشرة."
                                )
                            },
                            color = spec.textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (!hasAudioPermission) {
                                Button(
                                    onClick = onRequestAudioPermission,
                                    colors = ButtonDefaults.buttonColors(containerColor = spec.primary),
                                    modifier = Modifier.testTag("grant_audio_permission_btn")
                                ) {
                                    Text(tr(isArabic, "Allow Access", "سماح"), color = Color.Black)
                                }
                            } else {
                                Button(
                                    onClick = onRescanLibrary,
                                    colors = ButtonDefaults.buttonColors(containerColor = spec.primary)
                                ) {
                                    Text(tr(isArabic, "Rescan", "إعادة الفحص"), color = Color.Black)
                                }
                            }
                            OutlinedButton(onClick = onImportAudioFile) {
                                Text(tr(isArabic, "Pick Audio File", "اختيار ملف صوتي"), color = spec.textPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Instant 15-Theme Switcher Strip
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tr(isArabic, "15 Design Identities", "15 هوية تصميمية"),
                        color = spec.textPrimary,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = tr(isArabic, "Customize →", "تخصيص ←"),
                        color = spec.primary,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onOpenThemeStudio)
                            .padding(6.dp)
                    )
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(SonaraThemeId.entries, key = { it.name }) { themeId ->
                        val previewSpec = SonaraThemeCatalog.defaultSpecFor(themeId, spec.isDark)
                        val isActive = themeId == spec.id
                        Column(
                            modifier = Modifier
                                .width(136.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(previewSpec.surface)
                                .border(
                                    width = if (isActive) 2.dp else 1.dp,
                                    color = if (isActive) spec.primary else previewSpec.borderColor.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { onSelectTheme(themeId) }
                                .padding(12.dp)
                                .testTag("quick_theme_${themeId.name.lowercase()}"),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(previewSpec.primary)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(previewSpec.secondary)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(previewSpec.background)
                                        .border(0.5.dp, previewSpec.textSecondary, RoundedCornerShape(4.dp))
                                )
                            }
                            Text(
                                text = tr(isArabic, themeId.displayNameEn, themeId.displayNameAr),
                                color = previewSpec.textPrimary,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isActive) tr(isArabic, "ACTIVE", "مفعّل") else tr(isArabic, "Apply", "تطبيق"),
                                color = previewSpec.primary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }

        // Recently Played Shelf
        if (recentlyPlayed.isNotEmpty()) {
            item {
                HorizontalTrackCarousel(
                    title = tr(isArabic, "Recently Played", "تم تشغيلها مؤخراً"),
                    tracks = recentlyPlayed,
                    spec = spec,
                    onPlayTrack = { t -> onPlayTrack(t, recentlyPlayed) }
                )
            }
        }

        // Favorites Shelf
        if (favorites.isNotEmpty()) {
            item {
                HorizontalTrackCarousel(
                    title = tr(isArabic, "Favorites", "المفضلة"),
                    tracks = favorites,
                    spec = spec,
                    onPlayTrack = { t -> onPlayTrack(t, favorites) }
                )
            }
        }

        // Most Played Shelf
        if (mostPlayed.isNotEmpty()) {
            item {
                HorizontalTrackCarousel(
                    title = tr(isArabic, "Most Played", "الأكثر استماعاً"),
                    tracks = mostPlayed,
                    spec = spec,
                    onPlayTrack = { t -> onPlayTrack(t, mostPlayed) }
                )
            }
        }

        // Recently Added List
        if (recentlyAdded.isNotEmpty()) {
            item {
                Text(
                    text = tr(isArabic, "Recently Added", "المضافة حديثاً"),
                    color = spec.textPrimary,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            items(recentlyAdded.take(10), key = { "recent_${it.id}" }) { track ->
                TrackRowItem(
                    track = track,
                    isCurrent = track.id == currentTrackId,
                    isSelected = false,
                    selectionMode = false,
                    spec = spec,
                    isArabic = isArabic,
                    onClick = { onPlayTrack(track, recentlyAdded) },
                    onLongClick = { onTrackOptions(track) },
                    onToggleFavorite = { onToggleFavorite(track.id) },
                    onMoreClick = { onTrackOptions(track) }
                )
            }
        }
    }
}

@Composable
private fun HorizontalTrackCarousel(
    title: String,
    tracks: List<AudioTrack>,
    spec: SonaraThemeSpec,
    onPlayTrack: (AudioTrack) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            color = spec.textPrimary,
            style = MaterialTheme.typography.titleLarge
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(tracks, key = { "${title}_${it.id}" }) { track ->
                SonaraThemedCardSurface(
                    spec = spec,
                    modifier = Modifier
                        .width(136.dp)
                        .clickable { onPlayTrack(track) }
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        SonaraAlbumArt(
                            artUri = track.albumArtUri,
                            contentDescription = track.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(112.dp),
                            cornerRadius = (spec.cornerRadiusDp * 0.6f).coerceAtLeast(4f).dp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = track.title,
                            color = spec.textPrimary,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            color = spec.textSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
