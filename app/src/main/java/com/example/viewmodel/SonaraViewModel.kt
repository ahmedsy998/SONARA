package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AlbumArtPaletteExtractor
import com.example.data.AudioLibraryRepository
import com.example.data.CustomThemePresetEntity
import com.example.data.DynamicArtColors
import com.example.data.PlaybackPreferences
import com.example.data.PlaylistEntity
import com.example.data.PlaylistTrackCrossRef
import com.example.data.SonaraDatabase
import com.example.data.SonaraPreferencesRepository
import com.example.data.TrackStatsEntity
import com.example.model.AlbumGroup
import com.example.model.AppLanguage
import com.example.model.ArtistGroup
import com.example.model.AudioTechnicalInfo
import com.example.model.AudioTrack
import com.example.model.EqualizerConfig
import com.example.model.EqualizerPreset
import com.example.model.FolderGroup
import com.example.model.GenreGroup
import com.example.model.LibraryTab
import com.example.model.LrcLine
import com.example.model.SonaraThemeId
import com.example.model.SonaraThemeSpec
import com.example.model.SortOrder
import com.example.model.ThemeCustomizations
import com.example.model.VisualizerStyle
import com.example.model.WidgetCustomizations
import com.example.player.PlaybackController
import com.example.player.PlaybackUiState
import com.example.player.SonaraAudioBridge
import com.example.theme.SonaraThemeCatalog
import com.example.widget.WidgetUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlaylistWithTracks(
    val playlist: PlaylistEntity,
    val tracks: List<AudioTrack>
)

class SonaraViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SonaraDatabase.getInstance(application)
    private val trackStatsDao = db.trackStatsDao()
    private val playlistDao = db.playlistDao()
    private val historyDao = db.playbackHistoryDao()
    private val presetDao = db.customThemePresetDao()

    val prefsRepo = SonaraPreferencesRepository(application)
    val libraryRepo = AudioLibraryRepository(application, trackStatsDao)
    val playbackController = PlaybackController(application, prefsRepo, trackStatsDao, historyDao)
    private val visualizerEngine = SonaraAudioBridge.getVisualizerEngine(application)

    private val _rawScannedTracks = MutableStateFlow<List<AudioTrack>>(emptyList())
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _hasAudioPermission = MutableStateFlow(false)
    val hasAudioPermission: StateFlow<Boolean> = _hasAudioPermission.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedLibraryTab = MutableStateFlow(LibraryTab.SONGS)
    val selectedLibraryTab: StateFlow<LibraryTab> = _selectedLibraryTab.asStateFlow()

    private val _selectedTrackIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedTrackIds: StateFlow<Set<Long>> = _selectedTrackIds.asStateFlow()

    private val _dynamicArtColors = MutableStateFlow<DynamicArtColors?>(null)
    val dynamicArtColors: StateFlow<DynamicArtColors?> = _dynamicArtColors.asStateFlow()

    private val _previewThemeCustomizations = MutableStateFlow<ThemeCustomizations?>(null)
    val previewThemeCustomizations: StateFlow<ThemeCustomizations?> = _previewThemeCustomizations.asStateFlow()

    private val _currentLyricsRaw = MutableStateFlow<String?>(null)
    val currentLyricsRaw: StateFlow<String?> = _currentLyricsRaw.asStateFlow()

    private val _currentLrcLines = MutableStateFlow<List<LrcLine>>(emptyList())
    val currentLrcLines: StateFlow<List<LrcLine>> = _currentLrcLines.asStateFlow()

    private val _currentTrackTechInfo = MutableStateFlow(AudioTechnicalInfo())
    val currentTrackTechInfo: StateFlow<AudioTechnicalInfo> = _currentTrackTechInfo.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val playbackState: StateFlow<PlaybackUiState> = playbackController.state
    val fftBands: StateFlow<FloatArray> = visualizerEngine.fftBands
    val waveform: StateFlow<FloatArray> = visualizerEngine.waveform
    val isLiveVisualizerActive: StateFlow<Boolean> = visualizerEngine.isActive

    val themeCustomizations: StateFlow<ThemeCustomizations> = prefsRepo.themeCustomizations
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeCustomizations())

    val widgetCustomizations: StateFlow<WidgetCustomizations> = prefsRepo.widgetCustomizations
        .stateIn(viewModelScope, SharingStarted.Eagerly, WidgetCustomizations())

    val equalizerConfig: StateFlow<EqualizerConfig> = prefsRepo.equalizerConfig
        .stateIn(viewModelScope, SharingStarted.Eagerly, EqualizerConfig())

    val playbackPrefs: StateFlow<PlaybackPreferences> = prefsRepo.playbackPreferences
        .stateIn(viewModelScope, SharingStarted.Eagerly, PlaybackPreferences())

    val customThemePresets: StateFlow<List<CustomThemePresetEntity>> = presetDao.observePresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allStatsMap: StateFlow<Map<Long, TrackStatsEntity>> = trackStatsDao.observeAllStats()
        .combine(_rawScannedTracks) { stats, _ -> stats.associateBy { it.trackId } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val allTracks: StateFlow<List<AudioTrack>> = combine(
        _rawScannedTracks,
        trackStatsDao.observeAllStats(),
        playbackPrefs
    ) { raw, statsList, prefs ->
        val statsMap = statsList.associateBy { it.trackId }
        val merged = raw.map { track ->
            val st = statsMap[track.id]
            if (st == null) track else track.copy(
                title = st.customTitle?.takeIf { it.isNotBlank() } ?: track.title,
                artist = st.customArtist?.takeIf { it.isNotBlank() } ?: track.artist,
                album = st.customAlbum?.takeIf { it.isNotBlank() } ?: track.album,
                genre = st.customGenre?.takeIf { it.isNotBlank() } ?: track.genre,
                playCount = st.playCount,
                lastPlayedAt = st.lastPlayedAt,
                isFavorite = st.isFavorite,
                customLyrics = st.customLyrics
            )
        }
        sortTracks(merged, prefs.sortOrder)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val albums: StateFlow<List<AlbumGroup>> = allTracks.combine(_searchQuery) { tracks, _ ->
        tracks.groupBy { it.albumId to it.album }.map { (key, list) ->
            AlbumGroup(
                id = key.first,
                name = key.second,
                artist = list.firstOrNull()?.artist ?: "Unknown Artist",
                artUri = list.firstOrNull { it.albumArtUri != null }?.albumArtUri,
                tracks = list.sortedBy { it.trackNumber }
            )
        }.sortedBy { it.name.lowercase() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artists: StateFlow<List<ArtistGroup>> = allTracks.combine(_searchQuery) { tracks, _ ->
        tracks.groupBy { it.artist }.map { (artist, list) ->
            ArtistGroup(
                name = artist,
                artUri = list.firstOrNull { it.albumArtUri != null }?.albumArtUri,
                tracks = list,
                albumCount = list.map { it.album }.distinct().size
            )
        }.sortedBy { it.name.lowercase() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val genres: StateFlow<List<GenreGroup>> = allTracks.combine(_searchQuery) { tracks, _ ->
        tracks.groupBy { it.genre }.map { (genre, list) ->
            GenreGroup(name = genre, tracks = list)
        }.sortedBy { it.name.lowercase() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders: StateFlow<List<FolderGroup>> = allTracks.combine(_searchQuery) { tracks, _ ->
        tracks.groupBy { it.folderPath to it.folderName }.map { (pair, list) ->
            FolderGroup(name = pair.second, path = pair.first, tracks = list)
        }.sortedBy { it.name.lowercase() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<AudioTrack>> = allTracks.combine(_searchQuery) { tracks, _ ->
        tracks.filter { it.isFavorite }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayed: StateFlow<List<AudioTrack>> = allTracks.combine(
        historyDao.observeRecentHistory()
    ) { tracks, history ->
        val map = tracks.associateBy { it.id }
        history.mapNotNull { map[it.trackId] }.distinctBy { it.id }.take(30)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostPlayed: StateFlow<List<AudioTrack>> = allTracks.combine(_searchQuery) { tracks, _ ->
        tracks.filter { it.playCount > 0 }.sortedByDescending { it.playCount }.take(30)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyAdded: StateFlow<List<AudioTrack>> = allTracks.combine(_searchQuery) { tracks, _ ->
        tracks.sortedByDescending { it.dateAddedSec }.take(30)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlistsWithTracks: StateFlow<List<PlaylistWithTracks>> = combine(
        playlistDao.observePlaylists(),
        playlistDao.observeAllPlaylistTracks(),
        allTracks
    ) { playlists, refs, tracks ->
        val trackMap = tracks.associateBy { it.id }
        val groupedRefs = refs.groupBy { it.playlistId }
        playlists.map { pl ->
            val plTracks = groupedRefs[pl.id]
                ?.sortedBy { it.sortOrder }
                ?.mapNotNull { trackMap[it.trackId] }
                ?: emptyList()
            PlaylistWithTracks(playlist = pl, tracks = plTracks)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _systemDarkMode = MutableStateFlow(true)
    val resolvedThemeSpec: StateFlow<SonaraThemeSpec> = combine(
        themeCustomizations,
        _previewThemeCustomizations,
        _systemDarkMode,
        _dynamicArtColors
    ) { saved, preview, sysDark, artColors ->
        SonaraThemeCatalog.resolveThemeSpec(preview ?: saved, sysDark, artColors)
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        SonaraThemeCatalog.defaultSpecFor(SonaraThemeId.GLASSMORPHISM_PRO, true)
    )

    init {
        playbackController.connect()

        // Keep PlaybackController synced with catalog updates
        viewModelScope.launch {
            allTracks.collect { tracks ->
                playbackController.updateTrackCatalog(tracks)
                if (tracks.isNotEmpty()) {
                    val prefs = prefsRepo.getPlaybackPreferencesSnapshot()
                    playbackController.restoreQueueIfEmpty(
                        tracks = tracks,
                        lastTrackId = prefs.lastTrackId,
                        lastPosMs = prefs.lastPositionMs,
                        lastQueueIds = prefs.lastQueueIds
                    )
                    playbackController.applySavedShuffleAndRepeat(prefs.shuffleEnabled, prefs.repeatMode)
                }
            }
        }

        // React to current track change -> extract palette, lyrics, and audio technical specs
        viewModelScope.launch {
            var lastTrackId = -1L
            playbackState.collect { st ->
                val track = st.currentTrack
                if (track != null && track.id != lastTrackId) {
                    lastTrackId = track.id
                    loadTrackDetails(track)
                }
            }
        }
    }

    private fun loadTrackDetails(track: AudioTrack) {
        viewModelScope.launch {
            _dynamicArtColors.value = AlbumArtPaletteExtractor.extractColors(getApplication(), track.albumArtUri)
        }
        viewModelScope.launch {
            val raw = libraryRepo.loadLyricsForTrack(track)
            _currentLyricsRaw.value = raw
            _currentLrcLines.value = libraryRepo.parseLrcLines(raw)
        }
        viewModelScope.launch {
            _currentTrackTechInfo.value = libraryRepo.loadTechnicalInfo(track)
        }
    }

    fun updateSystemDarkMode(isSystemDark: Boolean) {
        _systemDarkMode.value = isSystemDark
    }

    fun onPermissionStatusChanged(granted: Boolean) {
        _hasAudioPermission.value = granted
        if (granted) {
            rescanLibrary()
        }
    }

    fun rescanLibrary() {
        viewModelScope.launch {
            _isScanning.value = true
            val stats = trackStatsDao.observeAllStats().first().associateBy { it.trackId }
            val scanned = libraryRepo.scanLocalAudio(stats)
            _rawScannedTracks.value = scanned
            _isScanning.value = false
        }
    }

    fun importAndPlayDocumentUri(uri: Uri) {
        viewModelScope.launch {
            val imported = libraryRepo.loadTrackFromDocumentUri(uri)
            if (imported != null) {
                val existing = _rawScannedTracks.value.toMutableList()
                if (existing.none { it.contentUri == uri }) {
                    existing.add(0, imported)
                    _rawScannedTracks.value = existing
                }
                playbackController.playTracks(listOf(imported), 0)
            } else {
                showMessage("Unable to open selected audio file.")
            }
        }
    }

    fun playTrack(track: AudioTrack, queue: List<AudioTrack> = allTracks.value) {
        val list = if (queue.isEmpty()) listOf(track) else queue
        val idx = list.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playbackController.playTracks(list, idx)
    }

    fun shuffleAll() {
        val tracks = allTracks.value
        if (tracks.isEmpty()) return
        val shuffled = tracks.shuffled()
        playbackController.playTracks(shuffled, 0)
    }

    fun toggleFavorite(trackId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = trackStatsDao.getStatsForTrack(trackId)
            if (existing == null) {
                trackStatsDao.upsert(TrackStatsEntity(trackId = trackId, isFavorite = true))
            } else {
                trackStatsDao.setFavorite(trackId, !existing.isFavorite)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setLibraryTab(tab: LibraryTab) {
        _selectedLibraryTab.value = tab
    }

    fun toggleTrackSelection(trackId: Long) {
        val current = _selectedTrackIds.value.toMutableSet()
        if (!current.add(trackId)) current.remove(trackId)
        _selectedTrackIds.value = current
    }

    fun clearSelection() {
        _selectedTrackIds.value = emptySet()
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch(Dispatchers.IO) { prefsRepo.saveSortOrder(order) }
    }

    fun setGridLayout(isGrid: Boolean) {
        viewModelScope.launch(Dispatchers.IO) { prefsRepo.saveGridLayout(isGrid) }
    }

    fun createPlaylist(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            playlistDao.insertPlaylist(PlaylistEntity(name = trimmed))
        }
    }

    fun renamePlaylist(playlist: PlaylistEntity, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            playlistDao.updatePlaylist(playlist.copy(name = trimmed))
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            playlistDao.deletePlaylistTracks(playlistId)
            playlistDao.deletePlaylist(playlistId)
        }
    }

    fun addTracksToPlaylist(playlistId: Long, trackIds: Collection<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            var order = playlistDao.getMaxSortOrder(playlistId) + 1
            val refs = trackIds.map { tid ->
                PlaylistTrackCrossRef(playlistId = playlistId, trackId = tid, sortOrder = order++)
            }
            playlistDao.insertPlaylistTracks(refs)
            showMessage("Added ${refs.size} track(s) to playlist")
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            playlistDao.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    fun movePlaylistTrack(playlistId: Long, tracks: List<AudioTrack>, fromIdx: Int, toIdx: Int) {
        if (fromIdx !in tracks.indices || toIdx !in tracks.indices || fromIdx == toIdx) return
        val mutable = tracks.toMutableList()
        val item = mutable.removeAt(fromIdx)
        mutable.add(toIdx, item)
        viewModelScope.launch(Dispatchers.IO) {
            val refs = mutable.mapIndexed { index, track ->
                PlaylistTrackCrossRef(playlistId = playlistId, trackId = track.id, sortOrder = index)
            }
            playlistDao.insertPlaylistTracks(refs)
        }
    }

    fun saveMetadata(
        track: AudioTrack,
        title: String,
        artist: String,
        album: String,
        genre: String,
        lyrics: String?
    ) {
        viewModelScope.launch {
            libraryRepo.saveEditedMetadata(track, title, artist, album, genre, lyrics)
            if (playbackState.value.currentTrack?.id == track.id) {
                _currentLyricsRaw.value = lyrics
                _currentLrcLines.value = libraryRepo.parseLrcLines(lyrics)
            }
            showMessage("Metadata updated")
        }
    }

    fun shareTrack(track: AudioTrack) {
        libraryRepo.shareTrack(track)
    }

    fun setAsRingtone(track: AudioTrack) {
        val ok = libraryRepo.setAsRingtone(track)
        if (ok) {
            showMessage("Ringtone set to ${track.title}")
        } else {
            showMessage("Grant 'Modify system settings' permission to set ringtone")
        }
    }

    // Theme Studio actions
    fun selectTheme(themeId: SonaraThemeId) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = themeCustomizations.value.copy(
                themeId = themeId.name,
                useCustomColors = false,
                useCustomGradient = false
            )
            prefsRepo.saveThemeCustomizations(updated)
            _previewThemeCustomizations.value = null
        }
    }

    fun setThemePreview(preview: ThemeCustomizations?) {
        _previewThemeCustomizations.value = preview
    }

    fun applyThemeCustomizations(customizations: ThemeCustomizations) {
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepo.saveThemeCustomizations(customizations)
            _previewThemeCustomizations.value = null
            showMessage("Theme applied")
        }
    }

    fun restoreDefaultTheme() {
        viewModelScope.launch(Dispatchers.IO) {
            val default = ThemeCustomizations()
            prefsRepo.saveThemeCustomizations(default)
            _previewThemeCustomizations.value = null
            showMessage("Restored default SONARA theme")
        }
    }

    fun saveCustomThemePreset(name: String, customizations: ThemeCustomizations) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val jsonStr = prefsRepo.serializeTheme(customizations)
            presetDao.insertPreset(
                CustomThemePresetEntity(
                    name = trimmed,
                    baseThemeId = customizations.themeId,
                    serializedJson = jsonStr
                )
            )
            showMessage("Saved preset \"$trimmed\"")
        }
    }

    fun applyCustomThemePreset(preset: CustomThemePresetEntity) {
        val parsed = prefsRepo.deserializeTheme(preset.serializedJson) ?: return
        applyThemeCustomizations(parsed)
    }

    fun deleteCustomThemePreset(presetId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            presetDao.deletePreset(presetId)
        }
    }

    // Widget Studio
    fun updateWidgetCustomizations(customizations: WidgetCustomizations) {
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepo.saveWidgetCustomizations(customizations)
            WidgetUpdater.updateAllWidgets(getApplication(), WidgetUpdater.lastSnapshot, customizations)
        }
    }

    // Equalizer & Audio Settings
    fun updateEqualizerConfig(config: EqualizerConfig) {
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepo.saveEqualizerConfig(config)
            SonaraAudioBridge.effectManager.applyConfig(config)
        }
    }

    fun selectEqualizerPreset(preset: EqualizerPreset) {
        val current = equalizerConfig.value
        updateEqualizerConfig(
            current.copy(
                presetName = preset.name,
                bandLevelsDb = preset.tenBandDb
            )
        )
    }

    fun setVisualizerStyle(style: VisualizerStyle) {
        viewModelScope.launch(Dispatchers.IO) { prefsRepo.saveVisualizerStyle(style) }
    }

    fun setVisualizerEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) { prefsRepo.saveVisualizerEnabled(enabled) }
    }

    fun setVisualizerLiveAudio(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepo.saveVisualizerLiveAudio(enabled)
            playbackController.refreshLiveVisualizer(enabled)
        }
    }

    fun setPauseOnDisconnect(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) { prefsRepo.savePauseOnDisconnect(enabled) }
    }

    fun setGapless(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) { prefsRepo.saveGapless(enabled) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch(Dispatchers.IO) { prefsRepo.saveLanguage(language) }
    }

    fun showMessage(msg: String) {
        _statusMessage.value = msg
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    private fun sortTracks(tracks: List<AudioTrack>, order: SortOrder): List<AudioTrack> {
        return when (order) {
            SortOrder.TITLE_ASC -> tracks.sortedBy { it.title.lowercase() }
            SortOrder.TITLE_DESC -> tracks.sortedByDescending { it.title.lowercase() }
            SortOrder.ARTIST_ASC -> tracks.sortedBy { it.artist.lowercase() }
            SortOrder.ALBUM_ASC -> tracks.sortedBy { it.album.lowercase() }
            SortOrder.DATE_ADDED_DESC -> tracks.sortedByDescending { it.dateAddedSec }
            SortOrder.DURATION_DESC -> tracks.sortedByDescending { it.durationMs }
            SortOrder.MOST_PLAYED -> tracks.sortedByDescending { it.playCount }
        }
    }

    override fun onCleared() {
        playbackController.release()
        super.onCleared()
    }
}
