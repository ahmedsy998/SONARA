package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TrackStatsEntity::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class,
        PlaybackHistoryEntity::class,
        CustomThemePresetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SonaraDatabase : RoomDatabase() {
    abstract fun trackStatsDao(): TrackStatsDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playbackHistoryDao(): PlaybackHistoryDao
    abstract fun customThemePresetDao(): CustomThemePresetDao

    companion object {
        @Volatile
        private var INSTANCE: SonaraDatabase? = null

        fun getInstance(context: Context): SonaraDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SonaraDatabase::class.java,
                    "sonara_database"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
