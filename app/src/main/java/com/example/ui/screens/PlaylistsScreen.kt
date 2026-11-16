package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.PlaylistEntity
import com.example.model.AudioTrack
import com.example.model.SonaraThemeSpec
import com.example.theme.SonaraThemedCardSurface
import com.example.theme.horizontalScreenPadding
import com.example.theme.verticalItemSpacing
import com.example.ui.components.SonaraAlbumArt
import com.example.ui.components.TrackRowItem
import com.example.ui.util.tr
import com.example.viewmodel.PlaylistWithTracks

@Composable
fun PlaylistsScreen(
    playlists: List<PlaylistWithTracks>,
    favorites: List<AudioTrack>,
    recentlyPlayed: List<AudioTrack>,
    mostPlayed: List<AudioTrack>,
    currentTrackId: Long?,
    spec: SonaraThemeSpec,
    isArabic: Boolean,
    onCreatePlaylist: (String) -> Unit,
    onRenamePlaylist: (PlaylistEntity, String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onRemoveFromPlaylist: (Long, Long) -> Unit,
    onMovePlaylistTrack: (Long, List<AudioTrack>, Int, Int) -> Unit,
    onPlayTrack: (AudioTrack, List<AudioTrack>) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onTrackOptions: (AudioTrack) -> Unit
) {
    val hPad = spec.horizontalScreenPadding()
    val vSpace = spec.verticalItemSpacing()

    var showCreateDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<PlaylistEntity?>(null) }
    var openedPlaylistId by remember { mutableStateOf<Long?>(null) }
    var openedSmartTitle by remember { mutableStateOf<String?>(null) }
    var openedSmartTracks by remember { mutableStateOf<List<AudioTrack>>(emptyList()) }

    val openedPlaylist = playlists.firstOrNull { it.playlist.id == openedPlaylistId }

    BackHandler(enabled = openedPlaylist != null || openedSmartTitle != null) {
        openedPlaylistId = null
        openedSmartTitle = null
        openedSmartTracks = emptyList()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("playlists_screen")
    ) {
        if (openedPlaylist != null) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = hPad, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { openedPlaylistId = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr(isArabic, "Back", "رجوع"), tint = spec.textPrimary)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(openedPlaylist.playlist.name, color = spec.textPrimary, style = MaterialTheme.typography.titleLarge)
                        Text("${openedPlaylist.tracks.size} ${tr(isArabic, "tracks", "مقاطع")}", color = spec.textSecondary)
                    }
                    if (openedPlaylist.tracks.isNotEmpty()) {
                        Button(
                            onClick = { onPlayTrack(openedPlaylist.tracks.first(), openedPlaylist.tracks) },
                            colors = ButtonDefaults.buttonColors(containerColor = spec.primary)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.Black)
                            Text(tr(isArabic, "Play", "تشغيل"), color = Color.Black)
                        }
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(start = hPad, end = hPad, bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(vSpace)
                ) {
                    itemsIndexed(openedPlaylist.tracks, key = { idx, t -> "${t.id}_$idx" }) { index, track ->
                        SonaraThemedCardSurface(spec = spec, modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPlayTrack(track, openedPlaylist.tracks) }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SonaraAlbumArt(
                                    artUri = track.albumArtUri,
                                    contentDescription = track.title,
                                    modifier = Modifier.size(46.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(track.title, color = spec.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                                    Text(track.artist, color = spec.textSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                }
                                IconButton(
                                    onClick = {
                                        if (index > 0) onMovePlaylistTrack(openedPlaylist.playlist.id, openedPlaylist.tracks, index, index - 1)
                                    },
                                    enabled = index > 0
                                ) {
                                    Icon(Icons.Filled.ArrowUpward, contentDescription = tr(isArabic, "Move up", "تحريك لأعلى"), tint = spec.textSecondary)
                                }
                                IconButton(
                                    onClick = {
                                        if (index < openedPlaylist.tracks.lastIndex) onMovePlaylistTrack(openedPlaylist.playlist.id, openedPlaylist.tracks, index, index + 1)
                                    },
                                    enabled = index < openedPlaylist.tracks.lastIndex
                                ) {
                                    Icon(Icons.Filled.ArrowDownward, contentDescription = tr(isArabic, "Move down", "تحريك لأسفل"), tint = spec.textSecondary)
                                }
                                IconButton(onClick = { onRemoveFromPlaylist(openedPlaylist.playlist.id, track.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = tr(isArabic, "Remove", "إزالة"), tint = spec.primary)
                                }
                            }
                        }
                    }
                }
            }
        } else if (openedSmartTitle != null) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = hPad, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { openedSmartTitle = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr(isArabic, "Back", "رجوع"), tint = spec.textPrimary)
                    }
                    Text(openedSmartTitle.orEmpty(), color = spec.textPrimary, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    if (openedSmartTracks.isNotEmpty()) {
                        TextButton(onClick = { onPlayTrack(openedSmartTracks.first(), openedSmartTracks) }) {
                            Text(tr(isArabic, "Play All", "تشغيل الكل"), color = spec.primary)
                        }
                    }
                }
                LazyColumn(
                    contentPadding = PaddingValues(start = hPad, end = hPad, bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(vSpace)
                ) {
                    items(openedSmartTracks, key = { it.id }) { track ->
                        TrackRowItem(
                            track = track,
                            isCurrent = track.id == currentTrackId,
                            isSelected = false,
                            selectionMode = false,
                            spec = spec,
                            isArabic = isArabic,
                            onClick = { onPlayTrack(track, openedSmartTracks) },
                            onLongClick = { onTrackOptions(track) },
                            onToggleFavorite = { onToggleFavorite(track.id) },
                            onMoreClick = { onTrackOptions(track) }
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = hPad, end = hPad, top = 12.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(vSpace + 4.dp)
            ) {
                item {
                    Text(
                        text = tr(isArabic, "Playlists & Collections", "قوائم التشغيل والمجموعات"),
                        color = spec.textPrimary,
                        style = MaterialTheme.typography.headlineLarge
                    )
                }

                // Smart Collections
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SmartCollectionCard(
                            icon = Icons.Filled.Favorite,
                            title = tr(isArabic, "Favorites", "المفضلة"),
                            count = favorites.size,
                            spec = spec,
                            onClick = {
                                openedSmartTitle = tr(isArabic, "Favorites", "المفضلة")
                                openedSmartTracks = favorites
                            }
                        )
                        SmartCollectionCard(
                            icon = Icons.Filled.History,
                            title = tr(isArabic, "Recently Played", "تم تشغيلها مؤخراً"),
                            count = recentlyPlayed.size,
                            spec = spec,
                            onClick = {
                                openedSmartTitle = tr(isArabic, "Recently Played", "تم تشغيلها مؤخراً")
                                openedSmartTracks = recentlyPlayed
                            }
                        )
                        SmartCollectionCard(
                            icon = Icons.Filled.TrendingUp,
                            title = tr(isArabic, "Most Played", "الأكثر استماعاً"),
                            count = mostPlayed.size,
                            spec = spec,
                            onClick = {
                                openedSmartTitle = tr(isArabic, "Most Played", "الأكثر استماعاً")
                                openedSmartTracks = mostPlayed
                            }
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = tr(isArabic, "Custom Playlists (${playlists.size})", "قوائمي (${playlists.size})"),
                        color = spec.primary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (playlists.isEmpty()) {
                    item {
                        SonaraThemedCardSurface(spec = spec, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = tr(isArabic, "No custom playlists yet. Tap + to create one.", "لا توجد قوائم تشغيل بعد. اضغط + لإنشاء قائمة."),
                                    color = spec.textSecondary
                                )
                            }
                        }
                    }
                } else {
                    items(playlists, key = { it.playlist.id }) { item ->
                        SonaraThemedCardSurface(
                            spec = spec,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { openedPlaylistId = item.playlist.id }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.QueueMusic, contentDescription = null, tint = spec.primary, modifier = Modifier.size(30.dp))
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.playlist.name, color = spec.textPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("${item.tracks.size} ${tr(isArabic, "songs", "أغاني")}", color = spec.textSecondary, style = MaterialTheme.typography.bodyMedium)
                                }
                                IconButton(onClick = { renameTarget = item.playlist }) {
                                    Icon(Icons.Filled.Edit, contentDescription = tr(isArabic, "Rename", "إعادة تسمية"), tint = spec.textSecondary)
                                }
                                IconButton(onClick = { onDeletePlaylist(item.playlist.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = tr(isArabic, "Delete", "حذف"), tint = spec.primary)
                                }
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = spec.primary,
                contentColor = Color.Black,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 148.dp)
                    .testTag("create_playlist_fab")
            ) {
                Icon(Icons.Filled.Add, contentDescription = tr(isArabic, "Create Playlist", "إنشاء قائمة"))
            }
        }
    }

    if (showCreateDialog) {
        PlaylistNameDialog(
            title = tr(isArabic, "New Playlist", "قائمة تشغيل جديدة"),
            initialName = "",
            isArabic = isArabic,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name ->
                onCreatePlaylist(name)
                showCreateDialog = false
            }
        )
    }

    renameTarget?.let { target ->
        PlaylistNameDialog(
            title = tr(isArabic, "Rename Playlist", "إعادة تسمية القائمة"),
            initialName = target.name,
            isArabic = isArabic,
            onDismiss = { renameTarget = null },
            onConfirm = { newName ->
                onRenamePlaylist(target, newName)
                renameTarget = null
            }
        )
    }
}

@Composable
private fun SmartCollectionCard(
    icon: ImageVector,
    title: String,
    count: Int,
    spec: SonaraThemeSpec,
    onClick: () -> Unit
) {
    SonaraThemedCardSurface(
        spec = spec,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = spec.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Text(title, color = spec.textPrimary, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text("$count", color = spec.primary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PlaylistNameDialog(
    title: String,
    initialName: String,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(tr(isArabic, "Playlist name", "اسم القائمة")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onConfirm(name) }) {
                Text(tr(isArabic, "Save", "حفظ"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(tr(isArabic, "Cancel", "إلغاء"))
            }
        }
    )
}
