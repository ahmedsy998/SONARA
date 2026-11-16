package com.example.player

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.example.R
import com.example.data.AudioLibraryRepository
import com.example.data.SonaraDatabase
import com.example.data.SonaraPreferencesRepository
import com.example.model.AppLanguage
import com.example.model.AudioTrack
import com.example.ui.util.localizedFor
import com.example.widget.WidgetPlaybackSnapshot
import com.example.widget.WidgetUpdater
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class SonaraPlaybackService : MediaLibraryService() {

    companion object {
        const val ACTION_EXTERNAL_COMMAND = "com.example.sonara.ACTION_EXTERNAL_COMMAND"
        private const val ROOT_ID = "sonara_root"
        private const val NODE_ALL_SONGS = "sonara_all_songs"
        private const val NODE_FAVORITES = "sonara_favorites"
        private const val NODE_ALBUMS = "sonara_albums"
        private const val NODE_ARTISTS = "sonara_artists"
    }

    private lateinit var player: ExoPlayer
    private var mediaLibrarySession: MediaLibrarySession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private lateinit var prefsRepo: SonaraPreferencesRepository
    private lateinit var libraryRepo: AudioLibraryRepository
    private var widgetTickerJob: Job? = null
    private var cachedTracks: List<AudioTrack> = emptyList()
    @Volatile
    private var currentLanguage: AppLanguage = AppLanguage.SYSTEM

    override fun onCreate() {
        super.onCreate()
        prefsRepo = SonaraPreferencesRepository(applicationContext)
        val db = SonaraDatabase.getInstance(applicationContext)
        libraryRepo = AudioLibraryRepository(applicationContext, db.trackStatsDao())

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        player.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                SonaraAudioBridge.currentAudioSessionId = audioSessionId
                serviceScope.launch {
                    val eqConfig = prefsRepo.equalizerConfig.first()
                    SonaraAudioBridge.effectManager.attachToSession(audioSessionId, eqConfig)
                    val playPrefs = prefsRepo.getPlaybackPreferencesSnapshot()
                    SonaraAudioBridge.getVisualizerEngine(applicationContext)
                        .start(audioSessionId, playPrefs.visualizerLiveAudioEnabled && player.isPlaying)
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                serviceScope.launch {
                    val playPrefs = prefsRepo.getPlaybackPreferencesSnapshot()
                    val sessionId = player.audioSessionId
                    val viz = SonaraAudioBridge.getVisualizerEngine(applicationContext)
                    if (isPlaying && playPrefs.visualizerLiveAudioEnabled && sessionId > 0) {
                        viz.start(sessionId, true)
                    } else if (!isPlaying) {
                        viz.stop()
                    }
                    pushWidgetUpdate()
                }
                if (isPlaying) startWidgetProgressTicker() else stopWidgetProgressTicker()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                pushWidgetUpdate()
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                pushWidgetUpdate()
            }
        })

        // Observe playback preferences (disconnect behavior & active app language)
        serviceScope.launch {
            prefsRepo.playbackPreferences.collect { prefs ->
                val langChanged = currentLanguage != prefs.language
                currentLanguage = prefs.language
                player.setHandleAudioBecomingNoisy(prefs.pauseOnHeadphoneDisconnect)
                if (langChanged) {
                    pushWidgetUpdate()
                }
            }
        }

        // Observe equalizer config live
        serviceScope.launch {
            prefsRepo.equalizerConfig.collect { eq ->
                SonaraAudioBridge.effectManager.applyConfig(eq)
            }
        }

        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, com.example.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("open_player", true)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaLibrarySession = MediaLibrarySession.Builder(this, player, SonaraLibraryCallback())
            .setSessionActivity(sessionActivityPendingIntent)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_EXTERNAL_COMMAND) {
            ensureForegroundSafeguard()
            val cmd = intent.getStringExtra(WidgetUpdater.EXTRA_COMMAND)
            handleExternalWidgetCommand(cmd)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun ensureForegroundSafeguard() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && !player.isPlaying) {
            val locCtx = applicationContext.localizedFor(currentLanguage)
            val notification = androidx.core.app.NotificationCompat.Builder(this, "sonara_playback_channel")
                .setSmallIcon(R.drawable.ic_widget_play)
                .setContentTitle("SONARA")
                .setContentText(locCtx.getString(R.string.preparing_playback))
                .setSilent(true)
                .build()
            runCatching { startForeground(1001, notification) }
        }
    }

    private fun handleExternalWidgetCommand(cmd: String?) {
        if (cmd == null) return
        serviceScope.launch {
            if (player.mediaItemCount == 0) {
                restoreLastQueueIntoPlayer(autoPlay = cmd == WidgetUpdater.CMD_PLAY_PAUSE)
            }
            when (cmd) {
                WidgetUpdater.CMD_PLAY_PAUSE -> {
                    if (player.isPlaying) player.pause() else {
                        if (player.playbackState == Player.STATE_IDLE) player.prepare()
                        player.play()
                    }
                }
                WidgetUpdater.CMD_NEXT -> if (player.hasNextMediaItem()) player.seekToNextMediaItem()
                WidgetUpdater.CMD_PREV -> if (player.hasPreviousMediaItem()) player.seekToPreviousMediaItem() else player.seekTo(0L)
                WidgetUpdater.CMD_SHUFFLE -> player.shuffleModeEnabled = !player.shuffleModeEnabled
            }
            pushWidgetUpdate()
        }
    }

    private suspend fun restoreLastQueueIntoPlayer(autoPlay: Boolean) {
        val prefs = prefsRepo.getPlaybackPreferencesSnapshot()
        if (cachedTracks.isEmpty()) {
            cachedTracks = libraryRepo.scanLocalAudio()
        }
        if (cachedTracks.isEmpty()) return

        val queueTracks = if (prefs.lastQueueIds.isNotEmpty()) {
            val map = cachedTracks.associateBy { it.id }
            prefs.lastQueueIds.mapNotNull { map[it] }.ifEmpty { cachedTracks }
        } else {
            cachedTracks
        }

        val items = queueTracks.map { it.toMediaItem() }
        val startIdx = queueTracks.indexOfFirst { it.id == prefs.lastTrackId }.coerceAtLeast(0)
        player.setMediaItems(items, startIdx, prefs.lastPositionMs.coerceAtLeast(0L))
        player.prepare()
        if (autoPlay) player.play()
    }

    private fun startWidgetProgressTicker() {
        widgetTickerJob?.cancel()
        widgetTickerJob = serviceScope.launch {
            while (isActive && player.isPlaying) {
                pushWidgetUpdate()
                delay(2000L)
            }
        }
    }

    private fun stopWidgetProgressTicker() {
        widgetTickerJob?.cancel()
        widgetTickerJob = null
    }

    private fun pushWidgetUpdate() {
        val item = player.currentMediaItem
        val meta = item?.mediaMetadata
        val snapshot = WidgetPlaybackSnapshot(
            title = meta?.title?.toString()?.takeIf { it.isNotBlank() } ?: "",
            artist = meta?.artist?.toString()?.takeIf { it.isNotBlank() } ?: "",
            isPlaying = player.isPlaying,
            isShuffle = player.shuffleModeEnabled,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.coerceAtLeast(0L),
            artworkUri = meta?.artworkUri?.toString()
        )
        serviceScope.launch(Dispatchers.IO) {
            val widgetPrefs = prefsRepo.getWidgetCustomizationsSnapshot()
            val playPrefs = prefsRepo.getPlaybackPreferencesSnapshot()
            currentLanguage = playPrefs.language
            WidgetUpdater.updateAllWidgets(applicationContext, snapshot, widgetPrefs, playPrefs.language)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val p = player
        if (!p.playWhenReady || p.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        stopWidgetProgressTicker()
        SonaraAudioBridge.effectManager.release()
        SonaraAudioBridge.getVisualizerEngine(applicationContext).stop()
        mediaLibrarySession?.run {
            player.release()
            release()
            mediaLibrarySession = null
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    private inner class SonaraLibraryCallback : MediaLibrarySession.Callback {

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val rootItem = MediaItem.Builder()
                .setMediaId(ROOT_ID)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("SONARA")
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                        .build()
                )
                .build()
            return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val future = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()
            serviceScope.launch(Dispatchers.IO) {
                if (cachedTracks.isEmpty()) {
                    cachedTracks = libraryRepo.scanLocalAudio()
                }
                val locCtx = applicationContext.localizedFor(currentLanguage)
                val children: List<MediaItem> = when (parentId) {
                    ROOT_ID -> listOf(
                        browsableFolder(NODE_ALL_SONGS, locCtx.getString(R.string.auto_all_songs), MediaMetadata.MEDIA_TYPE_FOLDER_MIXED),
                        browsableFolder(NODE_FAVORITES, locCtx.getString(R.string.auto_favorites), MediaMetadata.MEDIA_TYPE_FOLDER_MIXED),
                        browsableFolder(NODE_ALBUMS, locCtx.getString(R.string.auto_albums), MediaMetadata.MEDIA_TYPE_FOLDER_ALBUMS),
                        browsableFolder(NODE_ARTISTS, locCtx.getString(R.string.auto_artists), MediaMetadata.MEDIA_TYPE_FOLDER_ARTISTS)
                    )
                    NODE_ALL_SONGS -> cachedTracks.map { it.toMediaItem() }
                    NODE_FAVORITES -> cachedTracks.filter { it.isFavorite }.map { it.toMediaItem() }
                    NODE_ALBUMS -> cachedTracks.groupBy { it.album }.map { (album, list) ->
                        browsableFolder("album:$album", album, MediaMetadata.MEDIA_TYPE_ALBUM, list.firstOrNull()?.albumArtUri?.toString())
                    }
                    NODE_ARTISTS -> cachedTracks.groupBy { it.artist }.map { (artist, _) ->
                        browsableFolder("artist:$artist", artist, MediaMetadata.MEDIA_TYPE_ARTIST)
                    }
                    else -> when {
                        parentId.startsWith("album:") -> {
                            val album = parentId.removePrefix("album:")
                            cachedTracks.filter { it.album == album }.map { it.toMediaItem() }
                        }
                        parentId.startsWith("artist:") -> {
                            val artist = parentId.removePrefix("artist:")
                            cachedTracks.filter { it.artist == artist }.map { it.toMediaItem() }
                        }
                        else -> emptyList()
                    }
                }
                val paged = children.drop(page * pageSize).take(pageSize.coerceAtLeast(1))
                future.set(LibraryResult.ofItemList(ImmutableList.copyOf(paged), params))
            }
            return future
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val future = SettableFuture.create<LibraryResult<MediaItem>>()
            serviceScope.launch(Dispatchers.IO) {
                if (cachedTracks.isEmpty()) cachedTracks = libraryRepo.scanLocalAudio()
                val match = cachedTracks.firstOrNull { it.id.toString() == mediaId }
                if (match != null) {
                    future.set(LibraryResult.ofItem(match.toMediaItem(), null))
                } else {
                    future.set(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
                }
            }
            return future
        }

        override fun onGetSearchResult(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val future = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()
            serviceScope.launch(Dispatchers.IO) {
                if (cachedTracks.isEmpty()) cachedTracks = libraryRepo.scanLocalAudio()
                val q = query.trim().lowercase()
                val results = cachedTracks.filter {
                    it.title.lowercase().contains(q) ||
                        it.artist.lowercase().contains(q) ||
                        it.album.lowercase().contains(q)
                }.drop(page * pageSize).take(pageSize.coerceAtLeast(1)).map { it.toMediaItem() }
                future.set(LibraryResult.ofItemList(ImmutableList.copyOf(results), params))
            }
            return future
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            val updated = mediaItems.map { item ->
                if (item.localConfiguration != null) {
                    item
                } else {
                    val match = cachedTracks.firstOrNull { it.id.toString() == item.mediaId }
                    match?.toMediaItem() ?: item
                }
            }.toMutableList()
            return Futures.immediateFuture(updated)
        }
    }

    private fun browsableFolder(
        id: String,
        title: String,
        mediaType: Int,
        artUri: String? = null
    ): MediaItem {
        val meta = MediaMetadata.Builder()
            .setTitle(title)
            .setIsBrowsable(true)
            .setIsPlayable(false)
            .setMediaType(mediaType)
            .apply {
                if (artUri != null) setArtworkUri(android.net.Uri.parse(artUri))
            }
            .build()
        return MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(meta)
            .build()
    }
}

fun AudioTrack.toMediaItem(): MediaItem {
    val extras = Bundle().apply {
        putLong("track_id", id)
        putString("folder_name", folderName)
        putString("genre", genre)
    }
    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .setAlbumTitle(album)
        .setArtworkUri(albumArtUri)
        .setIsBrowsable(false)
        .setIsPlayable(true)
        .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
        .setExtras(extras)
        .build()

    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(contentUri)
        .setMediaMetadata(metadata)
        .build()
}
