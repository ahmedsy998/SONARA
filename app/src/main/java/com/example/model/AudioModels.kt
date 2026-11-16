package com.example.model

import android.net.Uri
import kotlinx.serialization.Serializable

data class AudioTrack(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val genre: String,
    val folderName: String,
    val folderPath: String,
    val durationMs: Long,
    val dateAddedSec: Long,
    val sizeBytes: Long,
    val mimeType: String,
    val filePath: String,
    val contentUri: Uri,
    val albumArtUri: Uri?,
    val trackNumber: Int = 0,
    val playCount: Int = 0,
    val lastPlayedAt: Long = 0L,
    val isFavorite: Boolean = false,
    val customLyrics: String? = null
)

data class AlbumGroup(
    val id: Long,
    val name: String,
    val artist: String,
    val artUri: Uri?,
    val tracks: List<AudioTrack>
)

data class ArtistGroup(
    val name: String,
    val artUri: Uri?,
    val tracks: List<AudioTrack>,
    val albumCount: Int
)

data class GenreGroup(
    val name: String,
    val tracks: List<AudioTrack>
)

data class FolderGroup(
    val name: String,
    val path: String,
    val tracks: List<AudioTrack>
)

data class LrcLine(
    val timestampMs: Long,
    val text: String
)

enum class SortOrder(val labelEn: String, val labelAr: String) {
    TITLE_ASC("Title (A-Z)", "العنوان (أ-ي)"),
    TITLE_DESC("Title (Z-A)", "العنوان (ي-أ)"),
    ARTIST_ASC("Artist", "الفنان"),
    ALBUM_ASC("Album", "الألبوم"),
    DATE_ADDED_DESC("Recently Added", "المضاف حديثاً"),
    DURATION_DESC("Duration", "المدة"),
    MOST_PLAYED("Most Played", "الأكثر تشغيلاً")
}

enum class LibraryTab(val labelEn: String, val labelAr: String) {
    SONGS("Songs", "الأغاني"),
    ALBUMS("Albums", "الألبومات"),
    ARTISTS("Artists", "الفنانون"),
    GENRES("Genres", "الأنواع"),
    FOLDERS("Folders", "المجلدات"),
    RECENT("Recent", "الأخيرة"),
    MOST_PLAYED("Top Played", "الأكثر استماعاً"),
    FAVORITES("Favorites", "المفضلة")
}

enum class MainTab {
    HOME,
    LIBRARY,
    SEARCH,
    PLAYLISTS,
    SETTINGS
}

enum class VisualizerStyle(val labelEn: String, val labelAr: String) {
    SPECTRUM_BARS("Spectrum Bars", "أعمدة الطيف"),
    CIRCULAR_WAVES("Circular Waves", "موجات دائرية"),
    MINIMAL_WAVEFORM("Minimal Waveform", "موجة بسيطة"),
    NEON_PARTICLES("Neon Particles", "جزيئات نيون"),
    REACTIVE_ABSTRACT("Reactive Abstract", "تجريدي تفاعلي")
}

enum class EqualizerPreset(val labelEn: String, val labelAr: String, val tenBandDb: List<Int>) {
    NORMAL("Normal", "عادي", listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)),
    BASS_BOOST("Bass Boost", "تعزيز الجهير", listOf(7, 6, 5, 3, 1, 0, 0, 0, 1, 1)),
    CLASSICAL("Classical", "كلاسيكي", listOf(4, 3, 2, 1, -1, -1, 0, 2, 3, 4)),
    DANCE("Dance", "دانس", listOf(5, 4, 2, 0, 1, 3, 4, 3, 2, 1)),
    ELECTRONIC("Electronic", "إلكتروني", listOf(5, 4, 1, -1, -2, 1, 3, 4, 5, 4)),
    HIP_HOP("Hip-Hop", "هيب هوب", listOf(6, 5, 3, 1, -1, -1, 1, 2, 3, 4)),
    JAZZ("Jazz", "جاز", listOf(3, 2, 1, 2, -1, -1, 0, 2, 3, 4)),
    POP("Pop", "بوب", listOf(-1, 1, 3, 4, 3, 1, -1, -1, 1, 2)),
    ROCK("Rock", "روك", listOf(4, 3, 1, -1, -2, 0, 2, 4, 5, 5)),
    VOCAL("Vocal", "صوتي", listOf(-2, -1, 0, 2, 4, 5, 4, 2, 0, -1)),
    CUSTOM("Custom", "مخصص", listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0))
}

enum class RepeatModeState {
    OFF, ALL, ONE
}

enum class AppLanguage(val code: String, val label: String) {
    ENGLISH("en", "English"),
    ARABIC("ar", "العربية"),
    SYSTEM("system", "System / النظام")
}

@Serializable
data class EqualizerConfig(
    val enabled: Boolean = true,
    val presetName: String = EqualizerPreset.NORMAL.name,
    val bandLevelsDb: List<Int> = List(10) { 0 },
    val bassBoostStrength: Int = 200,
    val virtualizerStrength: Int = 150
)

data class AudioTechnicalInfo(
    val bitrateKbps: Int = 0,
    val sampleRateHz: Int = 0,
    val formatLabel: String = "AUDIO"
)
