package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.AlbumGroup
import com.example.model.ArtistGroup
import com.example.model.AudioTrack
import com.example.model.FolderGroup
import com.example.model.SonaraThemeSpec
import com.example.theme.SonaraThemedCardSurface
import com.example.theme.cardShape
import com.example.theme.horizontalScreenPadding
import com.example.theme.verticalItemSpacing
import com.example.ui.components.TrackRowItem
import com.example.ui.util.tr

private enum class SearchFilter { ALL, SONGS, ARTISTS, ALBUMS, FOLDERS }

@Composable
fun SearchScreen(
    query: String,
    allTracks: List<AudioTrack>,
    albums: List<AlbumGroup>,
    artists: List<ArtistGroup>,
    folders: List<FolderGroup>,
    currentTrackId: Long?,
    spec: SonaraThemeSpec,
    isArabic: Boolean,
    onQueryChange: (String) -> Unit,
    onPlayTrack: (AudioTrack, List<AudioTrack>) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onTrackOptions: (AudioTrack) -> Unit
) {
    val hPad = spec.horizontalScreenPadding()
    val vSpace = spec.verticalItemSpacing()
    var filter by remember { mutableStateOf(SearchFilter.ALL) }

    val q = query.trim().lowercase()
    val matchingTracks = remember(q, allTracks) {
        if (q.isEmpty()) allTracks.take(15) else allTracks.filter {
            it.title.lowercase().contains(q) ||
                it.artist.lowercase().contains(q) ||
                it.album.lowercase().contains(q) ||
                it.folderName.lowercase().contains(q)
        }
    }
    val matchingArtists = remember(q, artists) {
        if (q.isEmpty()) emptyList() else artists.filter { it.name.lowercase().contains(q) }
    }
    val matchingAlbums = remember(q, albums) {
        if (q.isEmpty()) emptyList() else albums.filter {
            it.name.lowercase().contains(q) || it.artist.lowercase().contains(q)
        }
    }
    val matchingFolders = remember(q, folders) {
        if (q.isEmpty()) emptyList() else folders.filter { it.name.lowercase().contains(q) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("search_screen")
    ) {
        Column(modifier = Modifier.padding(horizontal = hPad, vertical = 12.dp)) {
            Text(
                text = tr(isArabic, "Instant Search", "بحث فوري"),
                color = spec.textPrimary,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(tr(isArabic, "Songs, artists, albums, folders...", "أغاني، فنانون، ألبومات، مجلدات...")) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = spec.primary) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = spec.textSecondary)
                        }
                    }
                },
                singleLine = true,
                shape = spec.cardShape(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = spec.primary,
                    unfocusedBorderColor = spec.borderColor,
                    focusedTextColor = spec.textPrimary,
                    unfocusedTextColor = spec.textPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input")
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SearchFilter.entries) { f ->
                    val label = when (f) {
                        SearchFilter.ALL -> tr(isArabic, "All", "الكل")
                        SearchFilter.SONGS -> tr(isArabic, "Songs", "الأغاني")
                        SearchFilter.ARTISTS -> tr(isArabic, "Artists", "الفنانون")
                        SearchFilter.ALBUMS -> tr(isArabic, "Albums", "الألبومات")
                        SearchFilter.FOLDERS -> tr(isArabic, "Folders", "المجلدات")
                    }
                    FilterChip(
                        selected = filter == f,
                        onClick = { filter = f },
                        label = { Text(label) }
                    )
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(start = hPad, end = hPad, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(vSpace)
        ) {
            if ((filter == SearchFilter.ALL || filter == SearchFilter.ARTISTS) && matchingArtists.isNotEmpty()) {
                item {
                    Text(tr(isArabic, "Artists", "الفنانون"), color = spec.primary, style = MaterialTheme.typography.titleMedium)
                }
                items(matchingArtists, key = { "art_${it.name}" }) { artist ->
                    SonaraThemedCardSurface(
                        spec = spec,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                artist.tracks.firstOrNull()?.let { onPlayTrack(it, artist.tracks) }
                            }
                    ) {
                        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(artist.name, color = spec.textPrimary, style = MaterialTheme.typography.titleMedium)
                            Text("${artist.tracks.size} ${tr(isArabic, "songs", "أغاني")}", color = spec.textSecondary)
                        }
                    }
                }
            }

            if ((filter == SearchFilter.ALL || filter == SearchFilter.ALBUMS) && matchingAlbums.isNotEmpty()) {
                item {
                    Text(tr(isArabic, "Albums", "الألبومات"), color = spec.primary, style = MaterialTheme.typography.titleMedium)
                }
                items(matchingAlbums, key = { "alb_${it.id}_${it.name}" }) { album ->
                    SonaraThemedCardSurface(
                        spec = spec,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                album.tracks.firstOrNull()?.let { onPlayTrack(it, album.tracks) }
                            }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(album.name, color = spec.textPrimary, style = MaterialTheme.typography.titleMedium)
                            Text("${album.artist} • ${album.tracks.size} tracks", color = spec.textSecondary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            if ((filter == SearchFilter.ALL || filter == SearchFilter.FOLDERS) && matchingFolders.isNotEmpty()) {
                item {
                    Text(tr(isArabic, "Folders", "المجلدات"), color = spec.primary, style = MaterialTheme.typography.titleMedium)
                }
                items(matchingFolders, key = { "fld_${it.path}" }) { folder ->
                    SonaraThemedCardSurface(
                        spec = spec,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                folder.tracks.firstOrNull()?.let { onPlayTrack(it, folder.tracks) }
                            }
                    ) {
                        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(folder.name, color = spec.textPrimary, style = MaterialTheme.typography.titleMedium)
                            Text("${folder.tracks.size}", color = spec.textSecondary)
                        }
                    }
                }
            }

            if (filter == SearchFilter.ALL || filter == SearchFilter.SONGS) {
                item {
                    Text(
                        text = if (q.isEmpty()) tr(isArabic, "Quick Picks", "مقترحات سريعة") else tr(isArabic, "Songs (${matchingTracks.size})", "الأغاني (${matchingTracks.size})"),
                        color = spec.primary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(matchingTracks, key = { "trk_${it.id}" }) { track ->
                    TrackRowItem(
                        track = track,
                        isCurrent = track.id == currentTrackId,
                        isSelected = false,
                        selectionMode = false,
                        spec = spec,
                        isArabic = isArabic,
                        onClick = { onPlayTrack(track, matchingTracks) },
                        onLongClick = { onTrackOptions(track) },
                        onToggleFavorite = { onToggleFavorite(track.id) },
                        onMoreClick = { onTrackOptions(track) }
                    )
                }
            }
        }
    }
}
