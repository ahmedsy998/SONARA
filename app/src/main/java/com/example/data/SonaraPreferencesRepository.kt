package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppLanguage
import com.example.model.EqualizerConfig
import com.example.model.RepeatModeState
import com.example.model.SortOrder
import com.example.model.ThemeCustomizations
import com.example.model.VisualizerStyle
import com.example.model.WidgetCustomizations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.sonaraDataStore: DataStore<Preferences> by preferencesDataStore(name = "sonara_prefs")

data class PlaybackPreferences(
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatModeState = RepeatModeState.OFF,
    val gaplessEnabled: Boolean = true,
    val pauseOnHeadphoneDisconnect: Boolean = true,
    val visualizerStyle: VisualizerStyle = VisualizerStyle.SPECTRUM_BARS,
    val visualizerEnabled: Boolean = true,
    val visualizerLiveAudioEnabled: Boolean = false,
    val lastTrackId: Long = -1L,
    val lastPositionMs: Long = 0L,
    val lastQueueIds: List<Long> = emptyList(),
    val sortOrder: SortOrder = SortOrder.TITLE_ASC,
    val isGridLayout: Boolean = false,
    val language: AppLanguage = AppLanguage.ENGLISH
)

class SonaraPreferencesRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private object Keys {
        val THEME_JSON = stringPreferencesKey("theme_customizations_json")
        val WIDGET_JSON = stringPreferencesKey("widget_customizations_json")
        val EQUALIZER_JSON = stringPreferencesKey("equalizer_config_json")
        val SHUFFLE = booleanPreferencesKey("shuffle_enabled")
        val REPEAT_MODE = stringPreferencesKey("repeat_mode")
        val GAPLESS = booleanPreferencesKey("gapless_enabled")
        val PAUSE_ON_DISCONNECT = booleanPreferencesKey("pause_on_disconnect")
        val VISUALIZER_STYLE = stringPreferencesKey("visualizer_style")
        val VISUALIZER_ENABLED = booleanPreferencesKey("visualizer_enabled")
        val VISUALIZER_LIVE_AUDIO = booleanPreferencesKey("visualizer_live_audio")
        val LAST_TRACK_ID = longPreferencesKey("last_track_id")
        val LAST_POSITION_MS = longPreferencesKey("last_position_ms")
        val LAST_QUEUE_IDS = stringPreferencesKey("last_queue_ids")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val GRID_LAYOUT = booleanPreferencesKey("grid_layout")
        val LANGUAGE = stringPreferencesKey("app_language")
    }

    val themeCustomizations: Flow<ThemeCustomizations> = context.sonaraDataStore.data.map { prefs ->
        prefs[Keys.THEME_JSON]?.let { raw ->
            runCatching { json.decodeFromString<ThemeCustomizations>(raw) }.getOrNull()
        } ?: ThemeCustomizations()
    }

    val widgetCustomizations: Flow<WidgetCustomizations> = context.sonaraDataStore.data.map { prefs ->
        prefs[Keys.WIDGET_JSON]?.let { raw ->
            runCatching { json.decodeFromString<WidgetCustomizations>(raw) }.getOrNull()
        } ?: WidgetCustomizations()
    }

    val equalizerConfig: Flow<EqualizerConfig> = context.sonaraDataStore.data.map { prefs ->
        prefs[Keys.EQUALIZER_JSON]?.let { raw ->
            runCatching { json.decodeFromString<EqualizerConfig>(raw) }.getOrNull()
        } ?: EqualizerConfig()
    }

    val playbackPreferences: Flow<PlaybackPreferences> = context.sonaraDataStore.data.map { prefs ->
        val queueIds = prefs[Keys.LAST_QUEUE_IDS]
            ?.split(",")
            ?.mapNotNull { it.toLongOrNull() }
            ?: emptyList()

        PlaybackPreferences(
            shuffleEnabled = prefs[Keys.SHUFFLE] ?: false,
            repeatMode = prefs[Keys.REPEAT_MODE]?.let {
                runCatching { RepeatModeState.valueOf(it) }.getOrNull()
            } ?: RepeatModeState.OFF,
            gaplessEnabled = prefs[Keys.GAPLESS] ?: true,
            pauseOnHeadphoneDisconnect = prefs[Keys.PAUSE_ON_DISCONNECT] ?: true,
            visualizerStyle = prefs[Keys.VISUALIZER_STYLE]?.let {
                runCatching { VisualizerStyle.valueOf(it) }.getOrNull()
            } ?: VisualizerStyle.SPECTRUM_BARS,
            visualizerEnabled = prefs[Keys.VISUALIZER_ENABLED] ?: true,
            visualizerLiveAudioEnabled = prefs[Keys.VISUALIZER_LIVE_AUDIO] ?: false,
            lastTrackId = prefs[Keys.LAST_TRACK_ID] ?: -1L,
            lastPositionMs = prefs[Keys.LAST_POSITION_MS] ?: 0L,
            lastQueueIds = queueIds,
            sortOrder = prefs[Keys.SORT_ORDER]?.let {
                runCatching { SortOrder.valueOf(it) }.getOrNull()
            } ?: SortOrder.TITLE_ASC,
            isGridLayout = prefs[Keys.GRID_LAYOUT] ?: false,
            language = prefs[Keys.LANGUAGE]?.let { code ->
                AppLanguage.entries.firstOrNull { it.code == code }
            } ?: AppLanguage.ENGLISH
        )
    }

    suspend fun saveThemeCustomizations(customizations: ThemeCustomizations) {
        val encoded = json.encodeToString(customizations)
        context.sonaraDataStore.edit { it[Keys.THEME_JSON] = encoded }
    }

    suspend fun getWidgetCustomizationsSnapshot(): WidgetCustomizations {
        return widgetCustomizations.first()
    }

    suspend fun saveWidgetCustomizations(customizations: WidgetCustomizations) {
        val encoded = json.encodeToString(customizations)
        context.sonaraDataStore.edit { it[Keys.WIDGET_JSON] = encoded }
    }

    suspend fun saveEqualizerConfig(config: EqualizerConfig) {
        val encoded = json.encodeToString(config)
        context.sonaraDataStore.edit { it[Keys.EQUALIZER_JSON] = encoded }
    }

    suspend fun saveShuffle(enabled: Boolean) {
        context.sonaraDataStore.edit { it[Keys.SHUFFLE] = enabled }
    }

    suspend fun saveRepeatMode(mode: RepeatModeState) {
        context.sonaraDataStore.edit { it[Keys.REPEAT_MODE] = mode.name }
    }

    suspend fun saveGapless(enabled: Boolean) {
        context.sonaraDataStore.edit { it[Keys.GAPLESS] = enabled }
    }

    suspend fun savePauseOnDisconnect(enabled: Boolean) {
        context.sonaraDataStore.edit { it[Keys.PAUSE_ON_DISCONNECT] = enabled }
    }

    suspend fun saveVisualizerStyle(style: VisualizerStyle) {
        context.sonaraDataStore.edit { it[Keys.VISUALIZER_STYLE] = style.name }
    }

    suspend fun saveVisualizerEnabled(enabled: Boolean) {
        context.sonaraDataStore.edit { it[Keys.VISUALIZER_ENABLED] = enabled }
    }

    suspend fun saveVisualizerLiveAudio(enabled: Boolean) {
        context.sonaraDataStore.edit { it[Keys.VISUALIZER_LIVE_AUDIO] = enabled }
    }

    suspend fun saveLastPlaybackState(trackId: Long, positionMs: Long, queueIds: List<Long>) {
        context.sonaraDataStore.edit { prefs ->
            prefs[Keys.LAST_TRACK_ID] = trackId
            prefs[Keys.LAST_POSITION_MS] = positionMs
            prefs[Keys.LAST_QUEUE_IDS] = queueIds.take(250).joinToString(",")
        }
    }

    suspend fun getPlaybackPreferencesSnapshot(): PlaybackPreferences = playbackPreferences.first()

    suspend fun saveSortOrder(order: SortOrder) {
        context.sonaraDataStore.edit { it[Keys.SORT_ORDER] = order.name }
    }

    suspend fun saveGridLayout(isGrid: Boolean) {
        context.sonaraDataStore.edit { it[Keys.GRID_LAYOUT] = isGrid }
    }

    suspend fun saveLanguage(language: AppLanguage) {
        context.sonaraDataStore.edit { it[Keys.LANGUAGE] = language.code }
    }

    fun serializeTheme(customizations: ThemeCustomizations): String = json.encodeToString(customizations)

    fun deserializeTheme(serialized: String): ThemeCustomizations? =
        runCatching { json.decodeFromString<ThemeCustomizations>(serialized) }.getOrNull()
}
