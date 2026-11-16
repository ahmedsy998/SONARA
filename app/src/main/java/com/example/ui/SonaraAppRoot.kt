package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.AudioTrack
import com.example.model.MainTab
import com.example.model.VisualizerStyle
import com.example.theme.SonaraTheme
import com.example.theme.SonaraThemedBackground
import com.example.ui.components.AddToPlaylistDialog
import com.example.ui.components.MetadataEditorDialog
import com.example.ui.components.SonaraAdaptiveBottomNav
import com.example.ui.components.SonaraMiniPlayer
import com.example.ui.components.SonaraNavigationRail
import com.example.ui.components.TrackOptionsDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ThemeStudioScreen
import com.example.ui.util.isArabicLocale
import com.example.ui.util.layoutDirectionFor
import com.example.ui.util.tr
import com.example.viewmodel.SonaraViewModel
import kotlinx.coroutines.delay

@Composable
fun SonaraAppRoot(
    viewModel: SonaraViewModel,
    widthSizeClass: WindowWidthSizeClass,
    initialOpenPlayer: Boolean,
    incomingAudioUri: Uri?
) {
    val context = LocalContext.current
    val isSystemDark = isSystemInDarkTheme()
    LaunchedEffect(isSystemDark) {
        viewModel.updateSystemDarkMode(isSystemDark)
    }

    val spec by viewModel.resolvedThemeSpec.collectAsStateWithLifecycle()
    val playbackPrefs by viewModel.playbackPrefs.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val genres by viewModel.genres.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val recentlyAdded by viewModel.recentlyAdded.collectAsStateWithLifecycle()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsStateWithLifecycle()
    val mostPlayed by viewModel.mostPlayed.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val playlists by viewModel.playlistsWithTracks.collectAsStateWithLifecycle()
    val themeCustomizations by viewModel.themeCustomizations.collectAsStateWithLifecycle()
    val customThemePresets by viewModel.customThemePresets.collectAsStateWithLifecycle()
    val widgetCustomizations by viewModel.widgetCustomizations.collectAsStateWithLifecycle()
    val equalizerConfig by viewModel.equalizerConfig.collectAsStateWithLifecycle()
    val hasAudioPermission by viewModel.hasAudioPermission.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val libraryTab by viewModel.selectedLibraryTab.collectAsStateWithLifecycle()
    val selectedTrackIds by viewModel.selectedTrackIds.collectAsStateWithLifecycle()
    val fftBands by viewModel.fftBands.collectAsStateWithLifecycle()
    val waveform by viewModel.waveform.collectAsStateWithLifecycle()
    val isLiveVizActive by viewModel.isLiveVisualizerActive.collectAsStateWithLifecycle()
    val currentLyricsRaw by viewModel.currentLyricsRaw.collectAsStateWithLifecycle()
    val currentLrcLines by viewModel.currentLrcLines.collectAsStateWithLifecycle()
    val techInfo by viewModel.currentTrackTechInfo.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    val isArabic = isArabicLocale(playbackPrefs.language)
    val layoutDirection = layoutDirectionFor(isArabic)

    var showSplash by remember { mutableStateOf(!initialOpenPlayer && incomingAudioUri == null) }
    var activeTab by remember { mutableStateOf(MainTab.HOME) }
    var showThemeStudio by remember { mutableStateOf(false) }
    var showNowPlaying by remember { mutableStateOf(initialOpenPlayer) }
    var openEqHighlight by remember { mutableStateOf(false) }

    var optionsTrack by remember { mutableStateOf<AudioTrack?>(null) }
    var editMetadataTrack by remember { mutableStateOf<AudioTrack?>(null) }
    var playlistPickerTrackIds by remember { mutableStateOf<Collection<Long>?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        val msg = statusMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.clearStatusMessage()
    }

    // Audio & Notification permissions
    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    fun checkAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED
    }

    fun checkRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[audioPermission] == true || checkAudioPermission()
        viewModel.onPermissionStatusChanged(granted)
    }

    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setVisualizerLiveAudio(true)
        } else {
            viewModel.showMessage(tr(isArabic, "Audio capture permission denied", "تم رفض صلاحية التقاط الصوت"))
        }
    }

    val openAudioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.importAndPlayDocumentUri(it)
            showNowPlaying = true
        }
    }

    LaunchedEffect(Unit) {
        val hasAudio = checkAudioPermission()
        viewModel.onPermissionStatusChanged(hasAudio)
        if (!hasAudio) {
            val reqs = mutableListOf(audioPermission)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                reqs.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionsLauncher.launch(reqs.toTypedArray())
        }
    }

    LaunchedEffect(incomingAudioUri) {
        if (incomingAudioUri != null) {
            viewModel.importAndPlayDocumentUri(incomingAudioUri)
            showNowPlaying = true
        }
    }

    LaunchedEffect(showSplash) {
        if (showSplash) {
            delay(850L)
            showSplash = false
        }
    }

    BackHandler(enabled = showThemeStudio || activeTab != MainTab.HOME) {
        when {
            showThemeStudio -> showThemeStudio = false
            activeTab != MainTab.HOME -> activeTab = MainTab.HOME
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        SonaraTheme(spec = spec, isArabic = isArabic) {
            Box(modifier = Modifier.fillMaxSize()) {
                SonaraThemedBackground(spec = spec) {
                    val useRail = widthSizeClass != WindowWidthSizeClass.Compact

                    Scaffold(
                        containerColor = Color.Transparent,
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                            if (!useRail && !showThemeStudio && !showNowPlaying) {
                                Column {
                                    SonaraMiniPlayer(
                                        track = playbackState.currentTrack,
                                        isPlaying = playbackState.isPlaying,
                                        positionMs = playbackState.positionMs,
                                        durationMs = playbackState.durationMs,
                                        spec = spec,
                                        isArabic = isArabic,
                                        onOpenPlayer = { showNowPlaying = true },
                                        onPlayPause = { viewModel.playbackController.togglePlayPause() },
                                        onNext = { viewModel.playbackController.skipNext() }
                                    )
                                    SonaraAdaptiveBottomNav(
                                        selectedTab = activeTab,
                                        spec = spec,
                                        isArabic = isArabic,
                                        onSelectTab = {
                                            activeTab = it
                                            openEqHighlight = false
                                        }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .padding(top = innerPadding.calculateTopPadding() * 0f)
                        ) {
                            if (useRail && !showThemeStudio && !showNowPlaying) {
                                SonaraNavigationRail(
                                    selectedTab = activeTab,
                                    spec = spec,
                                    isArabic = isArabic,
                                    onSelectTab = { activeTab = it }
                                )
                            }

                            Box(modifier = Modifier.weight(1f)) {
                                if (showThemeStudio) {
                                    ThemeStudioScreen(
                                        savedCustomizations = themeCustomizations,
                                        savedPresets = customThemePresets,
                                        activeSpec = spec,
                                        isArabic = isArabic,
                                        onPreviewChange = viewModel::setThemePreview,
                                        onApplyTheme = {
                                            viewModel.applyThemeCustomizations(it)
                                            showThemeStudio = false
                                        },
                                        onRestoreDefault = viewModel::restoreDefaultTheme,
                                        onSavePreset = viewModel::saveCustomThemePreset,
                                        onApplyPreset = {
                                            viewModel.applyCustomThemePreset(it)
                                            showThemeStudio = false
                                        },
                                        onDeletePreset = viewModel::deleteCustomThemePreset,
                                        onBack = { showThemeStudio = false }
                                    )
                                } else {
                                    when (activeTab) {
                                        MainTab.HOME -> HomeScreen(
                                            allTracks = allTracks,
                                            recentlyPlayed = recentlyPlayed,
                                            mostPlayed = mostPlayed,
                                            favorites = favorites,
                                            recentlyAdded = recentlyAdded,
                                            currentTrackId = playbackState.currentTrack?.id,
                                            hasAudioPermission = hasAudioPermission,
                                            isScanning = isScanning,
                                            spec = spec,
                                            isArabic = isArabic,
                                            onRequestAudioPermission = {
                                                val reqs = mutableListOf(audioPermission)
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                    reqs.add(Manifest.permission.POST_NOTIFICATIONS)
                                                }
                                                permissionsLauncher.launch(reqs.toTypedArray())
                                            },
                                            onImportAudioFile = {
                                                openAudioPickerLauncher.launch(arrayOf("audio/*"))
                                            },
                                            onRescanLibrary = viewModel::rescanLibrary,
                                            onShuffleAll = viewModel::shuffleAll,
                                            onPlayTrack = viewModel::playTrack,
                                            onToggleFavorite = viewModel::toggleFavorite,
                                            onTrackOptions = { optionsTrack = it },
                                            onSelectTheme = viewModel::selectTheme,
                                            onOpenThemeStudio = { showThemeStudio = true }
                                        )

                                        MainTab.LIBRARY -> LibraryScreen(
                                            selectedTab = libraryTab,
                                            sortOrder = playbackPrefs.sortOrder,
                                            isGridLayout = playbackPrefs.isGridLayout,
                                            allTracks = allTracks,
                                            albums = albums,
                                            artists = artists,
                                            genres = genres,
                                            folders = folders,
                                            recentlyAdded = recentlyAdded,
                                            mostPlayed = mostPlayed,
                                            favorites = favorites,
                                            currentTrackId = playbackState.currentTrack?.id,
                                            selectedTrackIds = selectedTrackIds,
                                            spec = spec,
                                            isArabic = isArabic,
                                            onSelectTab = viewModel::setLibraryTab,
                                            onSelectSortOrder = viewModel::setSortOrder,
                                            onToggleGridLayout = viewModel::setGridLayout,
                                            onImportAudioFile = { openAudioPickerLauncher.launch(arrayOf("audio/*")) },
                                            onPlayTrack = viewModel::playTrack,
                                            onToggleSelection = viewModel::toggleTrackSelection,
                                            onClearSelection = viewModel::clearSelection,
                                            onBatchAddToPlaylist = { ids -> playlistPickerTrackIds = ids },
                                            onShareTrack = viewModel::shareTrack,
                                            onToggleFavorite = viewModel::toggleFavorite,
                                            onTrackOptions = { optionsTrack = it }
                                        )

                                        MainTab.SEARCH -> SearchScreen(
                                            query = searchQuery,
                                            allTracks = allTracks,
                                            albums = albums,
                                            artists = artists,
                                            folders = folders,
                                            currentTrackId = playbackState.currentTrack?.id,
                                            spec = spec,
                                            isArabic = isArabic,
                                            onQueryChange = viewModel::setSearchQuery,
                                            onPlayTrack = viewModel::playTrack,
                                            onToggleFavorite = viewModel::toggleFavorite,
                                            onTrackOptions = { optionsTrack = it }
                                        )

                                        MainTab.PLAYLISTS -> PlaylistsScreen(
                                            playlists = playlists,
                                            favorites = favorites,
                                            recentlyPlayed = recentlyPlayed,
                                            mostPlayed = mostPlayed,
                                            currentTrackId = playbackState.currentTrack?.id,
                                            spec = spec,
                                            isArabic = isArabic,
                                            onCreatePlaylist = viewModel::createPlaylist,
                                            onRenamePlaylist = viewModel::renamePlaylist,
                                            onDeletePlaylist = viewModel::deletePlaylist,
                                            onRemoveFromPlaylist = viewModel::removeTrackFromPlaylist,
                                            onMovePlaylistTrack = viewModel::movePlaylistTrack,
                                            onPlayTrack = viewModel::playTrack,
                                            onToggleFavorite = viewModel::toggleFavorite,
                                            onTrackOptions = { optionsTrack = it }
                                        )

                                        MainTab.SETTINGS -> SettingsScreen(
                                            equalizerConfig = equalizerConfig,
                                            widgetCustomizations = widgetCustomizations,
                                            playbackPrefs = playbackPrefs,
                                            spec = spec,
                                            isArabic = isArabic,
                                            openEqualizerInitially = openEqHighlight,
                                            onOpenThemeStudio = { showThemeStudio = true },
                                            onUpdateEqualizer = viewModel::updateEqualizerConfig,
                                            onSelectEqPreset = viewModel::selectEqualizerPreset,
                                            onUpdateWidgetCustomizations = viewModel::updateWidgetCustomizations,
                                            onSetPauseOnDisconnect = viewModel::setPauseOnDisconnect,
                                            onSetVisualizerEnabled = viewModel::setVisualizerEnabled,
                                            onSetVisualizerStyle = viewModel::setVisualizerStyle,
                                            onToggleLiveVisualizer = { wantLive ->
                                                if (wantLive && !checkRecordPermission()) {
                                                    recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                } else {
                                                    viewModel.setVisualizerLiveAudio(wantLive)
                                                }
                                            },
                                            onSetLanguage = viewModel::setLanguage,
                                            onRescanLibrary = viewModel::rescanLibrary,
                                            onShowMessage = viewModel::showMessage
                                        )
                                    }
                                }

                                if (useRail && !showThemeStudio && !showNowPlaying && playbackState.currentTrack != null) {
                                    Box(modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp)) {
                                        SonaraMiniPlayer(
                                            track = playbackState.currentTrack,
                                            isPlaying = playbackState.isPlaying,
                                            positionMs = playbackState.positionMs,
                                            durationMs = playbackState.durationMs,
                                            spec = spec,
                                            isArabic = isArabic,
                                            onOpenPlayer = { showNowPlaying = true },
                                            onPlayPause = { viewModel.playbackController.togglePlayPause() },
                                            onNext = { viewModel.playbackController.skipNext() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Full-Screen Immersive Now Playing Overlay
                AnimatedVisibility(
                    visible = showNowPlaying,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    NowPlayingScreen(
                        playbackState = playbackState,
                        techInfo = techInfo,
                        lyricsRaw = currentLyricsRaw,
                        lrcLines = currentLrcLines,
                        fftBands = fftBands,
                        waveform = waveform,
                        visualizerStyle = playbackPrefs.visualizerStyle,
                        visualizerEnabled = playbackPrefs.visualizerEnabled,
                        isLiveVisualizerActive = isLiveVizActive,
                        hasAudioRecordPermission = checkRecordPermission(),
                        spec = spec,
                        isArabic = isArabic,
                        onDismiss = { showNowPlaying = false },
                        onPlayPause = { viewModel.playbackController.togglePlayPause() },
                        onNext = { viewModel.playbackController.skipNext() },
                        onPrevious = { viewModel.playbackController.skipPrevious() },
                        onSeek = { viewModel.playbackController.seekTo(it) },
                        onToggleShuffle = { viewModel.playbackController.toggleShuffle() },
                        onCycleRepeat = { viewModel.playbackController.cycleRepeatMode() },
                        onToggleFavorite = viewModel::toggleFavorite,
                        onPlayQueueIndex = { viewModel.playbackController.playQueueIndex(it) },
                        onMoveQueueItem = { f, t -> viewModel.playbackController.moveQueueItem(f, t) },
                        onRemoveQueueItem = { viewModel.playbackController.removeFromQueue(it) },
                        onSetSleepTimer = { viewModel.playbackController.setSleepTimer(it) },
                        onRequestLiveVisualizer = {
                            if (!checkRecordPermission()) {
                                recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                viewModel.setVisualizerLiveAudio(!playbackPrefs.visualizerLiveAudioEnabled)
                            }
                        },
                        onCycleVisualizerStyle = {
                            val styles = VisualizerStyle.entries
                            val next = styles[(styles.indexOf(playbackPrefs.visualizerStyle) + 1) % styles.size]
                            viewModel.setVisualizerStyle(next)
                        },
                        onOpenEqualizer = {
                            showNowPlaying = false
                            openEqHighlight = true
                            activeTab = MainTab.SETTINGS
                        },
                        onOpenTrackOptions = { optionsTrack = it },
                        onEditLyrics = { editMetadataTrack = it }
                    )
                }

                // Animated SONARA Brand Splash Screen
                AnimatedVisibility(
                    visible = showSplash,
                    enter = fadeIn(),
                    exit = fadeOut(animationSpec = tween(420))
                ) {
                    SonaraSplashOverlay()
                }
            }

            // Dialogs
            optionsTrack?.let { trk ->
                TrackOptionsDialog(
                    track = trk,
                    isArabic = isArabic,
                    onDismiss = { optionsTrack = null },
                    onPlayNext = { viewModel.playbackController.addToQueueNext(trk) },
                    onAddToPlaylist = { playlistPickerTrackIds = listOf(trk.id) },
                    onEditMetadata = { editMetadataTrack = trk },
                    onShare = { viewModel.shareTrack(trk) },
                    onSetRingtone = { viewModel.setAsRingtone(trk) }
                )
            }

            editMetadataTrack?.let { trk ->
                MetadataEditorDialog(
                    track = trk,
                    initialLyrics = trk.customLyrics ?: currentLyricsRaw,
                    isArabic = isArabic,
                    onDismiss = { editMetadataTrack = null },
                    onSave = { title, artist, album, genre, lyrics ->
                        viewModel.saveMetadata(trk, title, artist, album, genre, lyrics)
                    }
                )
            }

            playlistPickerTrackIds?.let { ids ->
                AddToPlaylistDialog(
                    playlists = playlists,
                    isArabic = isArabic,
                    onDismiss = {
                        playlistPickerTrackIds = null
                        viewModel.clearSelection()
                    },
                    onSelectPlaylist = { pid ->
                        viewModel.addTracksToPlaylist(pid, ids)
                        viewModel.clearSelection()
                    },
                    onCreatePlaylist = viewModel::createPlaylist
                )
            }
        }
    }
}

@Composable
private fun SonaraSplashOverlay() {
    val scale = remember { Animatable(0.84f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(350))
        scale.animateTo(1f, animationSpec = tween(450))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF131D3B), Color(0xFF0B0D17))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_sonara_fg),
                contentDescription = "SONARA Logo",
                modifier = Modifier
                    .size(116.dp)
                    .clip(RoundedCornerShape(28.dp))
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text = "SONARA",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "ADAPTIVE AUDIO ARCHITECTURE",
                color = Color(0xFF38BDF8),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.5.sp
            )
        }
    }
}
