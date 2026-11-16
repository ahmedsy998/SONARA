package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AudioTrack
import com.example.model.MainTab
import com.example.model.NavStyle
import com.example.model.SonaraThemeId
import com.example.model.SonaraThemeSpec
import com.example.theme.cardShape
import com.example.ui.util.tr

data class NavTabItem(
    val tab: MainTab,
    val labelEn: String,
    val labelAr: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)

val sonaraNavTabs = listOf(
    NavTabItem(MainTab.HOME, "Home", "الرئيسية", Icons.Filled.Home, Icons.Outlined.Home),
    NavTabItem(MainTab.LIBRARY, "Library", "المكتبة", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
    NavTabItem(MainTab.SEARCH, "Search", "بحث", Icons.Filled.Search, Icons.Outlined.Search),
    NavTabItem(MainTab.PLAYLISTS, "Playlists", "القوائم", Icons.Filled.QueueMusic, Icons.Outlined.QueueMusic),
    NavTabItem(MainTab.SETTINGS, "Settings", "الإعدادات", Icons.Filled.Settings, Icons.Outlined.Settings)
)

@Composable
fun SonaraMiniPlayer(
    track: AudioTrack?,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    spec: SonaraThemeSpec,
    isArabic: Boolean,
    onOpenPlayer: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = track != null) {
        if (track == null) return@AnimatedVisibility
        val progress = if (durationMs > 0L) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
        val shape = spec.cardShape(-4f)

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clip(shape)
                .background(spec.surfaceElevated.copy(alpha = 0.94f))
                .border(1.dp, spec.borderColor.copy(alpha = 0.65f), shape)
                .clickable(onClick = onOpenPlayer)
                .testTag("mini_player_bar")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SonaraAlbumArt(
                    artUri = track.albumArtUri,
                    contentDescription = track.title,
                    modifier = Modifier.size(44.dp),
                    cornerRadius = if (spec.id == SonaraThemeId.NOTHING_OS) 22.dp else 10.dp
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        color = spec.textPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.artist,
                        color = spec.textSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.testTag("mini_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = tr(isArabic, "Play or Pause", "تشغيل أو إيقاف"),
                        tint = spec.primary
                    )
                }
                IconButton(
                    onClick = onNext,
                    modifier = Modifier.testTag("mini_next_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = tr(isArabic, "Next", "التالي"),
                        tint = spec.textPrimary
                    )
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = spec.primary,
                trackColor = spec.borderColor.copy(alpha = 0.25f)
            )
        }
    }
}

@Composable
fun SonaraAdaptiveBottomNav(
    selectedTab: MainTab,
    spec: SonaraThemeSpec,
    isArabic: Boolean,
    onSelectTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val isFloatingPill = spec.navStyle in listOf(
        NavStyle.FLOATING_GLASS_PILL,
        NavStyle.AURORA_CAPSULE,
        NavStyle.HOLO_PRISM_DOCK
    )
    val isCyber = spec.navStyle == NavStyle.CYBER_CHAMFER_DOCK
    val isNothing = spec.navStyle == NavStyle.NOTHING_DOT_STRIP
    val isEditorial = spec.navStyle == NavStyle.EDITORIAL_INDEX_STRIP

    val outerPadding = if (isFloatingPill) 14.dp else 0.dp
    val navShape = when {
        isFloatingPill -> RoundedCornerShape(32.dp)
        isCyber -> CutCornerShape(topStart = 18.dp, topEnd = 18.dp)
        isEditorial -> RoundedCornerShape(0.dp)
        spec.navStyle == NavStyle.ONE_UI_ROUNDED_DOCK -> RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
        else -> RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
    }

    val containerBrush = when {
        isFloatingPill -> Brush.horizontalGradient(
            listOf(
                spec.surface.copy(alpha = 0.88f),
                spec.surfaceElevated.copy(alpha = 0.88f)
            )
        )
        else -> Brush.verticalGradient(
            listOf(
                spec.surface.copy(alpha = 0.96f),
                spec.backgroundSecondary.copy(alpha = 0.98f)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = outerPadding, vertical = if (isFloatingPill) 8.dp else 0.dp)
            .clip(navShape)
            .background(containerBrush)
            .border(1.dp, spec.borderColor.copy(alpha = 0.55f), navShape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            sonaraNavTabs.forEachIndexed { index, item ->
                val selected = selectedTab == item.tab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectTab(item.tab) }
                        .testTag("nav_tab_${item.tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isEditorial) {
                        Text(
                            text = "0${index + 1}",
                            color = if (selected) spec.primary else spec.textSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (selected && !isNothing) spec.primary.copy(alpha = 0.16f)
                                    else Color.Transparent
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (selected) item.activeIcon else item.inactiveIcon,
                                contentDescription = tr(isArabic, item.labelEn, item.labelAr),
                                tint = if (selected) spec.primary else spec.textSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = tr(isArabic, item.labelEn, item.labelAr),
                        color = if (selected) spec.primary else spec.textSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isNothing && selected) {
                        Spacer(Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(spec.primary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SonaraNavigationRail(
    selectedTab: MainTab,
    spec: SonaraThemeSpec,
    isArabic: Boolean,
    onSelectTab: (MainTab) -> Unit
) {
    NavigationRail(
        containerColor = spec.surface.copy(alpha = 0.94f),
        contentColor = spec.textPrimary
    ) {
        Spacer(Modifier.height(16.dp))
        sonaraNavTabs.forEach { item ->
            val selected = selectedTab == item.tab
            NavigationRailItem(
                selected = selected,
                onClick = { onSelectTab(item.tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.activeIcon else item.inactiveIcon,
                        contentDescription = tr(isArabic, item.labelEn, item.labelAr)
                    )
                },
                label = { Text(tr(isArabic, item.labelEn, item.labelAr)) }
            )
        }
    }
}
