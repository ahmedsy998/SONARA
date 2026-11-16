package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackStatsDao {
    @Query("SELECT * FROM track_stats")
    fun observeAllStats(): Flow<List<TrackStatsEntity>>

    @Query("SELECT * FROM track_stats WHERE trackId = :trackId")
    suspend fun getStatsForTrack(trackId: Long): TrackStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stats: TrackStatsEntity)

    @Query("UPDATE track_stats SET playCount = playCount + 1, lastPlayedAt = :timestamp WHERE trackId = :trackId")
    suspend fun incrementPlayCount(trackId: Long, timestamp: Long)

    @Query("UPDATE track_stats SET isFavorite = :isFavorite WHERE trackId = :trackId")
    suspend fun setFavorite(trackId: Long, isFavorite: Boolean)
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlist_tracks ORDER BY playlistId, sortOrder ASC")
    fun observeAllPlaylistTracks(): Flow<List<PlaylistTrackCrossRef>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun deletePlaylistTracks(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrack(crossRef: PlaylistTrackCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTracks(crossRefs: List<PlaylistTrackCrossRef>)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getMaxSortOrder(playlistId: Long): Int
}

@Dao
interface PlaybackHistoryDao {
    @Query("SELECT * FROM playback_history ORDER BY playedAt DESC LIMIT 100")
    fun observeRecentHistory(): Flow<List<PlaybackHistoryEntity>>

    @Insert
    suspend fun insertHistory(entry: PlaybackHistoryEntity)

    @Query("DELETE FROM playback_history")
    suspend fun clearHistory()
}

@Dao
interface CustomThemePresetDao {
    @Query("SELECT * FROM custom_theme_presets ORDER BY createdAt DESC")
    fun observePresets(): Flow<List<CustomThemePresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: CustomThemePresetEntity): Long

    @Query("DELETE FROM custom_theme_presets WHERE id = :id")
    suspend fun deletePreset(id: Long)
}
