package com.example.player

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.data.PlaybackHistoryDao
import com.example.data.PlaybackHistoryEntity
import com.example.data.SonaraPreferencesRepository
import com.example.data.TrackStatsDao
import com.example.data.TrackStatsEntity
import com.example.model.AudioTrack
import com.example.model.RepeatModeState
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlaybackUiState(
    val currentTrack: AudioTrack? = null,
    val queue: List<AudioTrack> = emptyList(),
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatModeState = RepeatModeState.OFF,
    val sleepTimerRemainingSec: Int = 0
)

class PlaybackController(
    private val context: Context,
    private val prefsRepo: SonaraPreferencesRepository,
    private val trackStatsDao: TrackStatsDao,
    private val historyDao: PlaybackHistoryDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private var knownTracksById: Map<Long, AudioTrack> = emptyMap()
    private var positionTickerJob: Job? = null
    private var sleepTimerJob: Job? = null

    private val _state = MutableStateFlow(PlaybackUiState())
    val state: StateFlow<PlaybackUiState> = _state.asStateFlow()

    fun connect() {
        if (controllerFuture != null) return
        val token = SessionToken(context, ComponentName(context, SonaraPlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                runCatching {
                    val controller = future.get()
                    mediaController = controller
                    attachControllerListener(controller)
                    syncFromController(controller)
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    fun updateTrackCatalog(tracks: List<AudioTrack>) {
        knownTracksById = tracks.associateBy { it.id }
        mediaController?.let { syncFromController(it) }
    }

    private fun attachControllerListener(controller: MediaController) {
        controller.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                syncFromController(controller)
                if (isPlaying) startPositionTicker() else stopPositionTicker()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                syncFromController(controller)
                val trackId = mediaItem?.mediaId?.toLongOrNull()
                if (trackId != null) {
                    recordTrackPlayed(trackId)
                }
            }

            override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
                syncFromController(controller)
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                syncFromController(controller)
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                syncFromController(controller)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                syncFromController(controller)
            }
        })
        if (controller.isPlaying) startPositionTicker()
    }

    private fun syncFromController(controller: MediaController) {
        val count = controller.mediaItemCount
        val queue = (0 until count).mapNotNull { idx ->
            val item = controller.getMediaItemAt(idx)
            val id = item.mediaId.toLongOrNull()
            val catalogMatch = id?.let { knownTracksById[it] }
            catalogMatch ?: item.toFallbackTrack()
        }

        val currentIdx = controller.currentMediaItemIndex.takeIf { it in queue.indices } ?: -1
        val currentTrack = queue.getOrNull(currentIdx)
        val repeat = when (controller.repeatMode) {
            Player.REPEAT_MODE_ONE -> RepeatModeState.ONE
            Player.REPEAT_MODE_ALL -> RepeatModeState.ALL
            else -> RepeatModeState.OFF
        }

        _state.value = _state.value.copy(
            currentTrack = currentTrack,
            queue = queue,
            currentIndex = currentIdx,
            isPlaying = controller.isPlaying,
            positionMs = controller.currentPosition.coerceAtLeast(0L),
            durationMs = controller.duration.takeIf { it > 0 } ?: (currentTrack?.durationMs ?: 0L),
            shuffleEnabled = controller.shuffleModeEnabled,
            repeatMode = repeat
        )
    }

    private fun startPositionTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = scope.launch {
            var saveTick = 0
            while (isActive) {
                val c = mediaController ?: break
                val pos = c.currentPosition.coerceAtLeast(0L)
                val dur = c.duration.takeIf { it > 0 } ?: (_state.value.currentTrack?.durationMs ?: 0L)
                _state.value = _state.value.copy(
                    positionMs = pos,
                    durationMs = dur,
                    isPlaying = c.isPlaying
                )
                saveTick++
                if (saveTick % 10 == 0) {
                    persistPlaybackPosition()
                }
                delay(400L)
            }
        }
    }

    private fun stopPositionTicker() {
        positionTickerJob?.cancel()
        positionTickerJob = null
        persistPlaybackPosition()
    }

    private fun persistPlaybackPosition() {
        val st = _state.value
        val trackId = st.currentTrack?.id ?: return
        val queueIds = st.queue.map { it.id }
        scope.launch(Dispatchers.IO) {
            prefsRepo.saveLastPlaybackState(trackId, st.positionMs, queueIds)
        }
    }

    private fun recordTrackPlayed(trackId: Long) {
        scope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val existing = trackStatsDao.getStatsForTrack(trackId)
            if (existing == null) {
                trackStatsDao.upsert(TrackStatsEntity(trackId = trackId, playCount = 1, lastPlayedAt = now))
            } else {
                trackStatsDao.incrementPlayCount(trackId, now)
            }
            historyDao.insertHistory(PlaybackHistoryEntity(trackId = trackId, playedAt = now))
        }
    }

    fun playTracks(tracks: List<AudioTrack>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        val controller = mediaController ?: return
        val safeIdx = startIndex.coerceIn(0, tracks.lastIndex)
        val mediaItems = tracks.map { it.toMediaItem() }
        controller.setMediaItems(mediaItems, safeIdx, 0L)
        controller.prepare()
        controller.play()
        syncFromController(controller)
        recordTrackPlayed(tracks[safeIdx].id)
        persistPlaybackPosition()
    }

    fun restoreQueueIfEmpty(tracks: List<AudioTrack>, lastTrackId: Long, lastPosMs: Long, lastQueueIds: List<Long>) {
        val controller = mediaController ?: return
        if (controller.mediaItemCount > 0 || tracks.isEmpty()) return

        val map = tracks.associateBy { it.id }
        val queue = if (lastQueueIds.isNotEmpty()) {
            lastQueueIds.mapNotNull { map[it] }.ifEmpty { tracks }
        } else tracks

        val startIdx = queue.indexOfFirst { it.id == lastTrackId }.coerceAtLeast(0)
        controller.setMediaItems(queue.map { it.toMediaItem() }, startIdx, lastPosMs.coerceAtLeast(0L))
        controller.prepare()
        controller.pause()
        syncFromController(controller)
    }

    fun togglePlayPause() {
        val c = mediaController ?: return
        if (c.isPlaying) {
            c.pause()
        } else {
            if (c.playbackState == Player.STATE_IDLE) c.prepare()
            c.play()
        }
    }

    fun skipNext() {
        val c = mediaController ?: return
        if (c.hasNextMediaItem()) {
            c.seekToNextMediaItem()
        } else if (c.mediaItemCount > 0) {
            c.seekTo(0, 0L)
        }
        if (!c.isPlaying) c.play()
    }

    fun skipPrevious() {
        val c = mediaController ?: return
        if (c.currentPosition > 4000L) {
            c.seekTo(0L)
        } else if (c.hasPreviousMediaItem()) {
            c.seekToPreviousMediaItem()
        } else {
            c.seekTo(0L)
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs.coerceAtLeast(0L))
        _state.value = _state.value.copy(positionMs = positionMs.coerceAtLeast(0L))
    }

    fun toggleShuffle() {
        val c = mediaController ?: return
        val next = !c.shuffleModeEnabled
        c.shuffleModeEnabled = next
        scope.launch(Dispatchers.IO) { prefsRepo.saveShuffle(next) }
    }

    fun cycleRepeatMode() {
        val c = mediaController ?: return
        val nextMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> RepeatModeState.ALL
            Player.REPEAT_MODE_ALL -> RepeatModeState.ONE
            else -> RepeatModeState.OFF
        }
        c.repeatMode = when (nextMode) {
            RepeatModeState.OFF -> Player.REPEAT_MODE_OFF
            RepeatModeState.ALL -> Player.REPEAT_MODE_ALL
            RepeatModeState.ONE -> Player.REPEAT_MODE_ONE
        }
        scope.launch(Dispatchers.IO) { prefsRepo.saveRepeatMode(nextMode) }
    }

    fun applySavedShuffleAndRepeat(shuffle: Boolean, repeat: RepeatModeState) {
        val c = mediaController ?: return
        c.shuffleModeEnabled = shuffle
        c.repeatMode = when (repeat) {
            RepeatModeState.OFF -> Player.REPEAT_MODE_OFF
            RepeatModeState.ALL -> Player.REPEAT_MODE_ALL
            RepeatModeState.ONE -> Player.REPEAT_MODE_ONE
        }
    }

    fun playQueueIndex(index: Int) {
        val c = mediaController ?: return
        if (index in 0 until c.mediaItemCount) {
            c.seekTo(index, 0L)
            c.play()
        }
    }

    fun addToQueueNext(track: AudioTrack) {
        val c = mediaController ?: return
        if (c.mediaItemCount == 0) {
            playTracks(listOf(track), 0)
        } else {
            val insertIdx = (c.currentMediaItemIndex + 1).coerceAtMost(c.mediaItemCount)
            c.addMediaItem(insertIdx, track.toMediaItem())
            syncFromController(c)
        }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        val c = mediaController ?: return
        if (fromIndex in 0 until c.mediaItemCount && toIndex in 0 until c.mediaItemCount && fromIndex != toIndex) {
            c.moveMediaItem(fromIndex, toIndex)
            syncFromController(c)
            persistPlaybackPosition()
        }
    }

    fun removeFromQueue(index: Int) {
        val c = mediaController ?: return
        if (index in 0 until c.mediaItemCount) {
            c.removeMediaItem(index)
            syncFromController(c)
            persistPlaybackPosition()
        }
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _state.value = _state.value.copy(sleepTimerRemainingSec = 0)
            return
        }
        var remaining = minutes * 60
        _state.value = _state.value.copy(sleepTimerRemainingSec = remaining)
        sleepTimerJob = scope.launch {
            while (remaining > 0 && isActive) {
                delay(1000L)
                remaining--
                _state.value = _state.value.copy(sleepTimerRemainingSec = remaining)
            }
            if (remaining <= 0) {
                mediaController?.pause()
                _state.value = _state.value.copy(sleepTimerRemainingSec = 0)
            }
        }
    }

    fun refreshLiveVisualizer(enabled: Boolean) {
        val sid = SonaraAudioBridge.currentAudioSessionId
        val viz = SonaraAudioBridge.getVisualizerEngine(context)
        if (enabled && sid > 0 && _state.value.isPlaying) {
            viz.start(sid, true)
        } else {
            viz.stop()
        }
    }

    fun release() {
        positionTickerJob?.cancel()
        sleepTimerJob?.cancel()
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        mediaController = null
    }
}

private fun MediaItem.toFallbackTrack(): AudioTrack {
    val meta = mediaMetadata
    val id = mediaId.toLongOrNull() ?: mediaId.hashCode().toLong()
    val uri = localConfiguration?.uri ?: android.net.Uri.EMPTY
    return AudioTrack(
        id = id,
        title = meta.title?.toString() ?: "Unknown Track",
        artist = meta.artist?.toString() ?: "Unknown Artist",
        album = meta.albumTitle?.toString() ?: "Unknown Album",
        albumId = -1L,
        genre = meta.extras?.getString("genre") ?: "Music",
        folderName = meta.extras?.getString("folder_name") ?: "Music",
        folderPath = "",
        durationMs = 0L,
        dateAddedSec = 0L,
        sizeBytes = 0L,
        mimeType = "audio/*",
        filePath = "",
        contentUri = uri,
        albumArtUri = meta.artworkUri
    )
}
