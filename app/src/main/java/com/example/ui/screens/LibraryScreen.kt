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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AlbumGroup
import com.example.model.ArtistGroup
import com.example.model.AudioTrack
import com.example.model.FolderGroup
import com.example.model.GenreGroup
import com.example.model.LibraryTab
import com.example.model.SonaraThemeSpec
import com.example.model.SortOrder
import com.example.theme.SonaraThemedCardSurface
import com.example.theme.horizontalScreenPadding
import com.example.theme.verticalItemSpacing
import com.example.ui.components.SonaraAlbumArt
import com.example.ui.components.TrackRowItem
import com.example.ui.util.tr

@Composable
fun LibraryScreen(
    selectedTab: LibraryTab,
    sortOrder: SortOrder,
    isGridLayout: Boolean,
    allTracks: List<AudioTrack>,
    albums: List<AlbumGroup>,
    artists: List<ArtistGroup>,
    genres: List<GenreGroup>,
    folders: List<FolderGroup>,
    recentlyAdded: List<AudioTrack>,
    mostPlayed: List<AudioTrack>,
    favorites: List<AudioTrack>,
    currentTrackId: Long?,
    selectedTrackIds: Set<Long>,
    spec: SonaraThemeSpec,
    isArabic: Boolean,
    onSelectTab: (LibraryTab) -> Unit,
    onSelectSortOrder: (SortOrder) -> Unit,
    onToggleGridLayout: (Boolean) -> Unit,
    onImportAudioFile: () -> Unit,
    onPlayTrack: (AudioTrack, List<AudioTrack>) -> Unit,
    onToggleSelection: (Long) -> Unit,
    onClearSelection: () -> Unit,
    onBatchAddToPlaylist: (Set<Long>) -> Unit,
    onShareTrack: (AudioTrack) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onTrackOptions: (AudioTrack) -> Unit
) {
    val hPad = spec.horizontalScreenPadding()
    val vSpace = spec.verticalItemSpacing()
    var showSortMenu by remember { mutableStateOf(false) }

    // Drill-down state for Album / Artist / Genre / Folder
    var drillTitle by remember { mutableStateOf<String?>(null) }
    var drillTracks by remember { mutableStateOf<List<AudioTrack>>(emptyList()) }

    BackHandler(enabled = drillTitle != null || selectedTrackIds.isNotEmpty()) {
        if (selectedTrackIds.isNotEmpty()) {
            onClearSelection()
        } else {
            drillTitle = null
            drillTracks = emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("library_screen")
    ) {
        // Header or Multi-select toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = hPad, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedTrackIds.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClearSelection) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear selection", tint = spec.textPrimary)
                    }
                    Text(
                        text = "${selectedTrackIds.size} ${tr(isArabic, "selected", "محدد")}",
                        color = spec.primary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row {
                    IconButton(onClick = { onBatchAddToPlaylist(selectedTrackIds) }) {
                        Icon(Icons.Filled.PlaylistAdd, contentDescription = "Add selected to playlist", tint = spec.primary)
                    }
                    if (selectedTrackIds.size == 1) {
                        val t = allTracks.firstOrNull { it.id == selectedTrackIds.first() }
                        if (t != null) {
                            IconButton(onClick = { onShareTrack(t) }) {
                                Icon(Icons.Filled.Share, contentDescription = "Share", tint = spec.textPrimary)
                            }
                        }
                    }
                }
            } else if (drillTitle != null) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IconButton(onClick = { drillTitle = null; drillTracks = emptyList() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = spec.textPrimary)
                    }
                    Text(
                        text = drillTitle.orEmpty(),
                        color = spec.textPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                TextButton(onClick = {
                    if (drillTracks.isNotEmpty()) onPlayTrack(drillTracks.first(), drillTracks)
                }) {
                    Text(tr(isArabic, "Play All", "تشغيل الكل"), color = spec.primary)
                }
            } else {
                Text(
                    text = tr(isArabic, "Library", "المكتبة"),
                    color = spec.textPrimary,
                    style = MaterialTheme.typography.headlineLarge
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onImportAudioFile, modifier = Modifier.testTag("import_audio_btn")) {
                        Icon(Icons.Filled.AudioFile, contentDescription = tr(isArabic, "Open File", "فتح ملف"), tint = spec.textPrimary)
                    }
                    IconButton(onClick = { onToggleGridLayout(!isGridLayout) }) {
                        Icon(
                            imageVector = if (isGridLayout) Icons.Filled.ViewList else Icons.Filled.GridView,
                            contentDescription = tr(isArabic, "Toggle View", "تبديل العرض"),
                            tint = spec.textPrimary
                        )
                    }
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Filled.Sort, contentDescription = tr(isArabic, "Sort", "ترتيب"), tint = spec.primary)
                        }
                        DropdownMenu(expanded = showSortMenu, onDismissRequest = { showSortMenu = false }) {
                            SortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = tr(isArabic, order.labelEn, order.labelAr),
                                            fontWeight = if (order == sortOrder) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        onSelectSortOrder(order)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Category tabs when not drilled down
        if (drillTitle == null) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = hPad),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(LibraryTab.entries) { tab ->
                    val selected = tab == selectedTab
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectTab(tab) },
                        label = { Text(tr(isArabic, tab.labelEn, tab.labelAr)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = spec.primary.copy(alpha = 0.22f),
                            selectedLabelColor = spec.primary,
                            labelColor = spec.textSecondary
                        )
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
        }

        val activeList: List<AudioTrack>? = when {
            drillTitle != null -> drillTracks
            selectedTab == LibraryTab.SONGS -> allTracks
            selectedTab == LibraryTab.RECENT -> recentlyAdded
            selectedTab == LibraryTab.MOST_PLAYED -> mostPlayed
            selectedTab == LibraryTab.FAVORITES -> favorites
            else -> null
        }

        if (activeList != null) {
            if (activeList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = tr(isArabic, "No songs in this section yet.", "لا توجد أغاني في هذا القسم بعد."),
                        color = spec.textSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else if (isGridLayout && drillTitle == null) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(start = hPad, end = hPad, top = 6.dp, bottom = 140.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(activeList, key = { it.id }) { track ->
                        SonaraThemedCardSurface(
                            spec = spec,
                            elevated = track.id == currentTrackId,
                            modifier = Modifier.clickable { onPlayTrack(track, activeList) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                SonaraAlbumArt(
                                    artUri = track.albumArtUri,
                                    contentDescription = track.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(128.dp),
                                    cornerRadius = (spec.cornerRadiusDp * 0.6f).coerceAtLeast(4f).dp
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = track.title,
                                    color = if (track.id == currentTrackId) spec.primary else spec.textPrimary,
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
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = hPad, end = hPad, top = 6.dp, bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(vSpace)
                ) {
                    items(activeList, key = { it.id }) { track ->
                        TrackRowItem(
                            track = track,
                            isCurrent = track.id == currentTrackId,
                            isSelected = track.id in selectedTrackIds,
                            selectionMode = selectedTrackIds.isNotEmpty(),
                            spec = spec,
                            isArabic = isArabic,
                            onClick = {
                                if (selectedTrackIds.isNotEmpty()) {
                                    onToggleSelection(track.id)
                                } else {
                                    onPlayTrack(track, activeList)
                                }
                            },
                            onLongClick = { onToggleSelection(track.id) },
                            onToggleFavorite = { onToggleFavorite(track.id) },
                            onMoreClick = { onTrackOptions(track) }
                        )
                    }
                }
            }
        } else {
            when (selectedTab) {
                LibraryTab.ALBUMS -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        contentPadding = PaddingValues(start = hPad, end = hPad, top = 6.dp, bottom = 140.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(albums, key = { "${it.id}_${it.name}" }) { album ->
                            SonaraThemedCardSurface(
                                spec = spec,
                                modifier = Modifier.clickable {
                                    drillTitle = album.name
                                    drillTracks = album.tracks
                                }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    SonaraAlbumArt(
                                        artUri = album.artUri,
                                        contentDescription = album.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(132.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(album.name, color = spec.textPrimary, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${album.artist} • ${album.tracks.size}", color = spec.textSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                LibraryTab.ARTISTS -> {
                    LazyColumn(
                        contentPadding = PaddingValues(start = hPad, end = hPad, top = 6.dp, bottom = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(vSpace)
                    ) {
                        items(artists, key = { it.name }) { artist ->
                            SonaraThemedCardSurface(
                                spec = spec,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        drillTitle = artist.name
                                        drillTracks = artist.tracks
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(Icons.Filled.Person, contentDescription = null, tint = spec.primary, modifier = Modifier.size(32.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(artist.name, color = spec.textPrimary, style = MaterialTheme.typography.titleMedium)
                                        Text("${artist.albumCount} ${tr(isArabic, "albums", "ألبومات")} • ${artist.tracks.size} ${tr(isArabic, "songs", "أغاني")}", color = spec.textSecondary, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }

                LibraryTab.GENRES -> {
                    LazyColumn(
                        contentPadding = PaddingValues(start = hPad, end = hPad, top = 6.dp, bottom = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(vSpace)
                    ) {
                        items(genres, key = { it.name }) { genre ->
                            SonaraThemedCardSurface(
                                spec = spec,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        drillTitle = genre.name
                                        drillTracks = genre.tracks
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(Icons.Filled.MusicNote, contentDescription = null, tint = spec.secondary)
                                    Column {
                                        Text(genre.name, color = spec.textPrimary, style = MaterialTheme.typography.titleMedium)
                                        Text("${genre.tracks.size} ${tr(isArabic, "tracks", "مقاطع")}", color = spec.textSecondary, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }

                LibraryTab.FOLDERS -> {
                    LazyColumn(
                        contentPadding = PaddingValues(start = hPad, end = hPad, top = 6.dp, bottom = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(vSpace)
                    ) {
                        items(folders, key = { it.path + it.name }) { folder ->
                            SonaraThemedCardSurface(
                                spec = spec,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        drillTitle = folder.name
                                        drillTracks = folder.tracks
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(Icons.Filled.Folder, contentDescription = null, tint = spec.primary)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(folder.name, color = spec.textPrimary, style = MaterialTheme.typography.titleMedium)
                                        Text("${folder.tracks.size} ${tr(isArabic, "tracks", "مقاطع")} • ${folder.path}", color = spec.textSecondary, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
                else -> Unit
            }
        }
    }
}
