package com.example.data

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.model.AudioTechnicalInfo
import com.example.model.AudioTrack
import com.example.model.LrcLine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AudioLibraryRepository(
    private val context: Context,
    private val trackStatsDao: TrackStatsDao
) {

    suspend fun scanLocalAudio(
        statsMap: Map<Long, TrackStatsEntity> = emptyMap()
    ): List<AudioTrack> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<AudioTrack>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projectionList = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TRACK
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            projectionList.add(MediaStore.Audio.Media.GENRE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projectionList.add(MediaStore.Audio.Media.RELATIVE_PATH)
        }

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 OR ${MediaStore.Audio.Media.MIME_TYPE} LIKE 'audio/%'"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        runCatching {
            context.contentResolver.query(
                collection,
                projectionList.toTypedArray(),
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val genreCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    cursor.getColumnIndex(MediaStore.Audio.Media.GENRE)
                } else -1
                val relPathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH)
                } else -1

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val duration = cursor.getLong(durationCol)
                    if (duration < 1000L) continue // skip tiny UI/notification clicks

                    val rawTitle = cursor.getString(titleCol).orEmpty().ifBlank { "Unknown Title" }
                    val rawArtist = cursor.getString(artistCol).orEmpty().let {
                        if (it.isBlank() || it == "<unknown>") "Unknown Artist" else it
                    }
                    val rawAlbum = cursor.getString(albumCol).orEmpty().let {
                        if (it.isBlank() || it == "<unknown>") "Unknown Album" else it
                    }
                    val albumId = cursor.getLong(albumIdCol)
                    val dateAdded = cursor.getLong(dateAddedCol)
                    val size = cursor.getLong(sizeCol)
                    val mime = cursor.getString(mimeCol).orEmpty().ifBlank { "audio/*" }
                    val filePath = cursor.getString(dataCol).orEmpty()
                    val trackNum = cursor.getInt(trackCol) % 1000
                    val rawGenre = if (genreCol >= 0) {
                        cursor.getString(genreCol).orEmpty().ifBlank { "Unknown Genre" }
                    } else "Music"

                    val folderPath = if (filePath.isNotBlank()) {
                        File(filePath).parent.orEmpty()
                    } else if (relPathCol >= 0) {
                        cursor.getString(relPathCol).orEmpty().trimEnd('/')
                    } else "Music"

                    val folderName = folderPath.substringAfterLast('/').ifBlank { "Root" }

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    val artUri = if (albumId > 0L) {
                        ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        )
                    } else null

                    val stats = statsMap[id]
                    tracks.add(
                        AudioTrack(
                            id = id,
                            title = stats?.customTitle?.takeIf { it.isNotBlank() } ?: rawTitle,
                            artist = stats?.customArtist?.takeIf { it.isNotBlank() } ?: rawArtist,
                            album = stats?.customAlbum?.takeIf { it.isNotBlank() } ?: rawAlbum,
                            albumId = albumId,
                            genre = stats?.customGenre?.takeIf { it.isNotBlank() } ?: rawGenre,
                            folderName = folderName,
                            folderPath = folderPath,
                            durationMs = duration,
                            dateAddedSec = dateAdded,
                            sizeBytes = size,
                            mimeType = mime,
                            filePath = filePath,
                            contentUri = contentUri,
                            albumArtUri = artUri,
                            trackNumber = trackNum,
                            playCount = stats?.playCount ?: 0,
                            lastPlayedAt = stats?.lastPlayedAt ?: 0L,
                            isFavorite = stats?.isFavorite ?: false,
                            customLyrics = stats?.customLyrics
                        )
                    )
                }
            }
        }
        tracks
    }

    suspend fun loadTrackFromDocumentUri(uri: Uri): AudioTrack? = withContext(Dispatchers.IO) {
        runCatching {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val titleMeta = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val artistMeta = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val albumMeta = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val genreMeta = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            val durationMeta = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            retriever.release()

            var displayName = "Local Audio"
            var sizeBytes = 0L
            context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                val nameIdx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = c.getColumnIndex(OpenableColumns.SIZE)
                if (c.moveToFirst()) {
                    if (nameIdx >= 0) displayName = c.getString(nameIdx) ?: displayName
                    if (sizeIdx >= 0) sizeBytes = c.getLong(sizeIdx)
                }
            }

            val cleanTitle = titleMeta?.takeIf { it.isNotBlank() }
                ?: displayName.substringBeforeLast('.')

            AudioTrack(
                id = uri.toString().hashCode().toLong(),
                title = cleanTitle,
                artist = artistMeta?.takeIf { it.isNotBlank() } ?: "Unknown Artist",
                album = albumMeta?.takeIf { it.isNotBlank() } ?: "Imported Files",
                albumId = -1L,
                genre = genreMeta?.takeIf { it.isNotBlank() } ?: "Music",
                folderName = "Imported",
                folderPath = uri.toString(),
                durationMs = durationMeta,
                dateAddedSec = System.currentTimeMillis() / 1000L,
                sizeBytes = sizeBytes,
                mimeType = context.contentResolver.getType(uri) ?: "audio/*",
                filePath = "",
                contentUri = uri,
                albumArtUri = null
            )
        }.getOrNull()
    }

    suspend fun loadTechnicalInfo(track: AudioTrack): AudioTechnicalInfo = withContext(Dispatchers.IO) {
        val ext = track.filePath.substringAfterLast('.', "").uppercase()
            .ifBlank { track.mimeType.substringAfter('/').uppercase() }
            .ifBlank { "AUDIO" }
        var bitrate = 0
        var sampleRate = 0
        runCatching {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, track.contentUri)
            bitrate = (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0) / 1000
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                sampleRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull() ?: 0
            }
            retriever.release()
        }
        AudioTechnicalInfo(
            bitrateKbps = bitrate,
            sampleRateHz = sampleRate,
            formatLabel = ext
        )
    }

    suspend fun loadLyricsForTrack(track: AudioTrack): String? = withContext(Dispatchers.IO) {
        if (!track.customLyrics.isNullOrBlank()) return@withContext track.customLyrics

        // 1. Check matching .lrc file in same folder
        if (track.filePath.isNotBlank()) {
            val lrcPath = track.filePath.substringBeforeLast('.') + ".lrc"
            val lrcFile = File(lrcPath)
            if (lrcFile.exists() && lrcFile.canRead()) {
                val content = runCatching { lrcFile.readText() }.getOrNull()
                if (!content.isNullOrBlank()) return@withContext content
            }
        }
        null
    }

    fun parseLrcLines(rawLyrics: String?): List<LrcLine> {
        if (rawLyrics.isNullOrBlank()) return emptyList()
        val regex = Regex("""\[(\d{1,2}):(\d{2})(?:\.(\d{1,3}))?](.*)""")
        val lines = mutableListOf<LrcLine>()
        rawLyrics.lines().forEach { line ->
            val match = regex.find(line.trim())
            if (match != null) {
                val min = match.groupValues[1].toLongOrNull() ?: 0L
                val sec = match.groupValues[2].toLongOrNull() ?: 0L
                val fracStr = match.groupValues[3]
                val millis = when (fracStr.length) {
                    1 -> (fracStr.toLongOrNull() ?: 0L) * 100L
                    2 -> (fracStr.toLongOrNull() ?: 0L) * 10L
                    3 -> fracStr.toLongOrNull() ?: 0L
                    else -> 0L
                }
                val text = match.groupValues[4].trim()
                if (text.isNotEmpty()) {
                    lines.add(LrcLine(timestampMs = min * 60_000L + sec * 1_000L + millis, text = text))
                }
            }
        }
        return lines.sortedBy { it.timestampMs }
    }

    suspend fun saveEditedMetadata(
        track: AudioTrack,
        title: String,
        artist: String,
        album: String,
        genre: String,
        lyrics: String?
    ) = withContext(Dispatchers.IO) {
        val existing = trackStatsDao.getStatsForTrack(track.id) ?: TrackStatsEntity(trackId = track.id)
        trackStatsDao.upsert(
            existing.copy(
                customTitle = title.trim(),
                customArtist = artist.trim(),
                customAlbum = album.trim(),
                customGenre = genre.trim(),
                customLyrics = lyrics?.trim()?.takeIf { it.isNotEmpty() }
            )
        )

        // Best-effort MediaStore metadata update if app owns or has write permission
        runCatching {
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.TITLE, title.trim())
                put(MediaStore.Audio.Media.ARTIST, artist.trim())
                put(MediaStore.Audio.Media.ALBUM, album.trim())
            }
            context.contentResolver.update(track.contentUri, values, null, null)
        }
    }

    fun shareTrack(track: AudioTrack, isArabic: Boolean = false) {
        val shareUri = runCatching {
            if (track.filePath.isNotBlank() && File(track.filePath).exists()) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    File(track.filePath)
                )
            } else {
                track.contentUri
            }
        }.getOrDefault(track.contentUri)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = track.mimeType.ifBlank { "audio/*" }
            putExtra(Intent.EXTRA_STREAM, shareUri)
            putExtra(Intent.EXTRA_SUBJECT, "${track.title} - ${track.artist}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooserTitle = if (isArabic) "مشاركة ${track.title}" else "Share ${track.title}"
        val chooser = Intent.createChooser(intent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun setAsRingtone(track: AudioTrack): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return false
        }
        return runCatching {
            RingtoneManager.setActualDefaultRingtoneUri(
                context,
                RingtoneManager.TYPE_RINGTONE,
                track.contentUri
            )
            true
        }.getOrDefault(false)
    }
}
