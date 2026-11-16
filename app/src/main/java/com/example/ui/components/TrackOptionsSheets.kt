package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueuePlayNext
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AudioTrack
import com.example.model.SonaraThemeSpec
import com.example.theme.SonaraThemedCardSurface
import com.example.ui.util.formatDuration
import com.example.ui.util.tr
import com.example.viewmodel.PlaylistWithTracks

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackRowItem(
    track: AudioTrack,
    isCurrent: Boolean,
    isSelected: Boolean,
    selectionMode: Boolean,
    spec: SonaraThemeSpec,
    isArabic: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SonaraThemedCardSurface(
        spec = spec,
        elevated = isCurrent || isSelected,
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .testTag("track_row_${track.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectionMode) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = if (isSelected) spec.primary else spec.textSecondary.copy(alpha = 0.4f),
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(24.dp)
                )
            }

            SonaraAlbumArt(
                artUri = track.albumArtUri,
                contentDescription = track.title,
                modifier = Modifier.size(50.dp),
                cornerRadius = (spec.cornerRadiusDp * 0.55f).coerceAtLeast(4f).dp
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = if (isCurrent) spec.primary else spec.textPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${track.artist} • ${formatDuration(track.durationMs)}",
                    color = spec.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (track.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = tr(isArabic, "Favorite", "مفضلة"),
                    tint = if (track.isFavorite) spec.primary else spec.textSecondary
                )
            }
            IconButton(onClick = onMoreClick) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = tr(isArabic, "More options", "خيارات إضافية"),
                    tint = spec.textSecondary
                )
            }
        }
    }
}

@Composable
fun TrackOptionsDialog(
    track: AudioTrack,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onEditMetadata: () -> Unit,
    onShare: () -> Unit,
    onSetRingtone: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = track.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OptionActionRow(Icons.Filled.QueuePlayNext, tr(isArabic, "Play Next", "تشغيل تالياً")) {
                    onPlayNext(); onDismiss()
                }
                OptionActionRow(Icons.Filled.PlaylistAdd, tr(isArabic, "Add to Playlist", "إضافة إلى قائمة تشغيل")) {
                    onAddToPlaylist(); onDismiss()
                }
                OptionActionRow(Icons.Filled.Edit, tr(isArabic, "Edit Metadata & Lyrics", "تعديل البيانات والكلمات")) {
                    onEditMetadata(); onDismiss()
                }
                OptionActionRow(Icons.Filled.Share, tr(isArabic, "Share Audio File", "مشاركة الملف الصوتي")) {
                    onShare(); onDismiss()
                }
                OptionActionRow(Icons.Filled.NotificationsActive, tr(isArabic, "Set as Ringtone", "تعيين كنغمة رنين")) {
                    onSetRingtone(); onDismiss()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(tr(isArabic, "Close", "إغلاق"))
            }
        }
    )
}

@Composable
private fun OptionActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun MetadataEditorDialog(
    track: AudioTrack,
    initialLyrics: String?,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, artist: String, album: String, genre: String, lyrics: String?) -> Unit
) {
    var title by remember { mutableStateOf(track.title) }
    var artist by remember { mutableStateOf(track.artist) }
    var album by remember { mutableStateOf(track.album) }
    var genre by remember { mutableStateOf(track.genre) }
    var lyrics by remember { mutableStateOf(initialLyrics.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr(isArabic, "Edit Track Metadata", "تعديل معلومات المقطع")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(tr(isArabic, "Title", "العنوان")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text(tr(isArabic, "Artist", "الفنان")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = album,
                    onValueChange = { album = it },
                    label = { Text(tr(isArabic, "Album", "الألبوم")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = genre,
                    onValueChange = { genre = it },
                    label = { Text(tr(isArabic, "Genre", "النوع")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lyrics,
                    onValueChange = { lyrics = it },
                    label = { Text(tr(isArabic, "Lyrics (LRC [mm:ss.xx] supported)", "الكلمات (يدعم LRC)")) },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(title, artist, album, genre, lyrics)
                onDismiss()
            }) {
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

@Composable
fun AddToPlaylistDialog(
    playlists: List<PlaylistWithTracks>,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onSelectPlaylist: (Long) -> Unit,
    onCreatePlaylist: (String) -> Unit
) {
    var newName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr(isArabic, "Add to Playlist", "إضافة إلى قائمة تشغيل")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text(tr(isArabic, "New playlist name", "اسم القائمة الجديدة")) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (newName.isNotBlank()) {
                                onCreatePlaylist(newName)
                                newName = ""
                            }
                        }
                    ) {
                        Text(tr(isArabic, "Create", "إنشاء"))
                    }
                }

                if (playlists.isEmpty()) {
                    Text(
                        text = tr(isArabic, "No playlists yet. Create one above.", "لا توجد قوائم بعد."),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(modifier = Modifier.height(180.dp)) {
                        items(playlists, key = { it.playlist.id }) { pl ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        onSelectPlaylist(pl.playlist.id)
                                        onDismiss()
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(pl.playlist.name, fontWeight = FontWeight.SemiBold)
                                Text("${pl.tracks.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(tr(isArabic, "Done", "تم"))
            }
        }
    )
}

@Composable
fun ColorPickerDialog(
    title: String,
    initialHex: Long,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onColorSelected: (Long) -> Unit
) {
    val initialColor = Color(initialHex)
    var red by remember { mutableFloatStateOf(initialColor.red) }
    var green by remember { mutableFloatStateOf(initialColor.green) }
    var blue by remember { mutableFloatStateOf(initialColor.blue) }

    val swatches = listOf(
        0xFF38BDF8L, 0xFF8B5CF6L, 0xFF22D3EEL, 0xFFFA2D48L,
        0xFFD71921L, 0xFF00F0FFL, 0xFFFF00AAL, 0xFF34D399L,
        0xFFD4AF37L, 0xFFF59E0BL, 0xFFE08E45L, 0xFF6366F1L,
        0xFF000000L, 0xFF0A1026L, 0xFF171A21L, 0xFFFFFFFFL
    )

    val composedColor = Color(red, green, blue, 1f)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(composedColor)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(swatches) { hex ->
                        val c = Color(hex)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                .clickable {
                                    red = c.red
                                    green = c.green
                                    blue = c.blue
                                }
                        )
                    }
                }

                Text("R: ${(red * 255).toInt()}  G: ${(green * 255).toInt()}  B: ${(blue * 255).toInt()}")
                Slider(
                    value = red,
                    onValueChange = { red = it },
                    colors = SliderDefaults.colors(thumbColor = Color.Red, activeTrackColor = Color.Red)
                )
                Slider(
                    value = green,
                    onValueChange = { green = it },
                    colors = SliderDefaults.colors(thumbColor = Color.Green, activeTrackColor = Color.Green)
                )
                Slider(
                    value = blue,
                    onValueChange = { blue = it },
                    colors = SliderDefaults.colors(thumbColor = Color.Blue, activeTrackColor = Color.Blue)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val argb = composedColor.toArgb().toLong() and 0xFFFFFFFFL
                onColorSelected(argb or 0xFF000000L)
                onDismiss()
            }) {
                Text(tr(isArabic, "Apply", "تطبيق"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(tr(isArabic, "Cancel", "إلغاء"))
            }
        }
    )
}
