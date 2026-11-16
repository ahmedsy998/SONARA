package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import androidx.core.graphics.ColorUtils
import com.example.MainActivity
import com.example.R
import com.example.model.SonaraThemeId
import com.example.model.WidgetCustomizations
import com.example.theme.SonaraThemeCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

data class WidgetPlaybackSnapshot(
    val title: String = "",
    val artist: String = "",
    val isPlaying: Boolean = false,
    val isShuffle: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val artworkUri: String? = null
)

object WidgetUpdater {

    const val ACTION_WIDGET_COMMAND = "com.example.sonara.WIDGET_ACTION"
    const val EXTRA_COMMAND = "widget_command"
    const val CMD_PLAY_PAUSE = "cmd_play_pause"
    const val CMD_NEXT = "cmd_next"
    const val CMD_PREV = "cmd_prev"
    const val CMD_SHUFFLE = "cmd_shuffle"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    var lastSnapshot: WidgetPlaybackSnapshot = WidgetPlaybackSnapshot()
        private set

    fun updateAllWidgets(
        context: Context,
        snapshot: WidgetPlaybackSnapshot,
        customizations: WidgetCustomizations,
        language: com.example.model.AppLanguage = com.example.model.AppLanguage.SYSTEM
    ) {
        lastSnapshot = snapshot
        scope.launch {
            val localizedCtx = com.example.ui.util.localizedForHelper(context, language)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val artBitmap = loadRoundedArtworkBitmap(context, snapshot.artworkUri, 192, 24f)

            val providers = listOf(
                SonaraCompactWidgetProvider::class.java to R.layout.widget_compact,
                SonaraGlassWidgetProvider::class.java to R.layout.widget_glass,
                SonaraLargeWidgetProvider::class.java to R.layout.widget_large,
                SonaraMinimalWidgetProvider::class.java to R.layout.widget_minimal
            )

            for ((providerClass, layoutRes) in providers) {
                val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, providerClass))
                if (ids.isEmpty()) continue
                val views = buildRemoteViews(
                    context = context,
                    localizedContext = localizedCtx,
                    isArabic = com.example.ui.util.isArabicLanguage(language),
                    layoutRes = layoutRes,
                    snapshot = snapshot,
                    customizations = customizations,
                    artBitmap = artBitmap
                )
                appWidgetManager.updateAppWidget(ids, views)
            }
        }
    }

    private fun buildRemoteViews(
        context: Context,
        localizedContext: Context,
        isArabic: Boolean,
        layoutRes: Int,
        snapshot: WidgetPlaybackSnapshot,
        customizations: WidgetCustomizations,
        artBitmap: Bitmap?
    ): RemoteViews {
        val views = RemoteViews(context.packageName, layoutRes)

        val themeId = runCatching { SonaraThemeId.valueOf(customizations.widgetThemeId) }
            .getOrDefault(SonaraThemeId.GLASSMORPHISM_PRO)
        val themeSpec = SonaraThemeCatalog.defaultSpecFor(themeId, isDark = true)

        val rawBg = customizations.backgroundColorHex.toInt()
        val alphaInt = (customizations.transparency.coerceIn(0.15f, 1f) * 255).toInt()
        val bgWithAlpha = ColorUtils.setAlphaComponent(rawBg, alphaInt)
        val accentInt = customizations.accentColorHex.toInt()
        val textPrimaryInt = customizations.textColorHex.toInt()
        val textSecondaryInt = ColorUtils.setAlphaComponent(textPrimaryInt, 175)

        val bgBitmap = renderWidgetBackgroundBitmap(
            width = 480,
            height = 240,
            fillColor = bgWithAlpha,
            borderColor = ColorUtils.setAlphaComponent(accentInt, 110),
            cornerRadiusPx = customizations.cornerRadiusDp * 2f,
            isGlass = layoutRes == R.layout.widget_glass || themeId == SonaraThemeId.GLASSMORPHISM_PRO
        )
        views.setImageViewBitmap(R.id.widget_bg_image, bgBitmap)

        val resolvedTitle = snapshot.title.ifBlank {
            localizedContext.getString(R.string.no_track_playing)
        }
        val resolvedArtist = snapshot.artist.ifBlank {
            localizedContext.getString(R.string.tap_to_open)
        }
        views.setTextViewText(R.id.widget_title, resolvedTitle)
        views.setTextViewText(R.id.widget_artist, resolvedArtist)
        views.setTextColor(R.id.widget_title, textPrimaryInt)
        views.setTextColor(R.id.widget_artist, textSecondaryInt)

        if (layoutRes == R.layout.widget_glass || layoutRes == R.layout.widget_large) {
            views.setTextColor(R.id.widget_badge, accentInt)
            val themeLabel = if (isArabic) themeSpec.id.displayNameAr else themeSpec.id.displayNameEn.uppercase()
            views.setTextViewText(R.id.widget_badge, "SONARA • $themeLabel")
        }

        // Artwork
        if (customizations.showArtwork) {
            views.setViewVisibility(R.id.widget_artwork, View.VISIBLE)
            if (artBitmap != null) {
                views.setImageViewBitmap(R.id.widget_artwork, artBitmap)
            } else {
                views.setImageViewResource(R.id.widget_artwork, R.drawable.img_default_album_art)
            }
        } else {
            views.setViewVisibility(R.id.widget_artwork, View.GONE)
        }

        // Progress bar (Glass & Large)
        if (layoutRes == R.layout.widget_glass || layoutRes == R.layout.widget_large) {
            val progressVal = if (snapshot.durationMs > 0) {
                ((snapshot.positionMs.toDouble() / snapshot.durationMs) * 1000).toInt().coerceIn(0, 1000)
            } else 0
            views.setViewVisibility(
                R.id.widget_progress,
                if (customizations.showProgress) View.VISIBLE else View.GONE
            )
            views.setProgressBar(R.id.widget_progress, 1000, progressVal, false)
        }

        if (layoutRes == R.layout.widget_large) {
            views.setTextViewText(R.id.widget_time_current, formatTime(snapshot.positionMs))
            views.setTextViewText(R.id.widget_time_total, formatTime(snapshot.durationMs))
            views.setTextColor(R.id.widget_time_current, textSecondaryInt)
            views.setTextColor(R.id.widget_time_total, textSecondaryInt)
            views.setInt(
                R.id.widget_btn_shuffle,
                "setColorFilter",
                if (snapshot.isShuffle) accentInt else textSecondaryInt
            )
            views.setOnClickPendingIntent(
                R.id.widget_btn_shuffle,
                commandPendingIntent(context, CMD_SHUFFLE, 104)
            )
        }

        // Play/pause & skip icons tinted nicely
        views.setImageViewResource(
            R.id.widget_btn_play_pause,
            if (snapshot.isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
        )
        views.setInt(R.id.widget_btn_play_pause, "setColorFilter", accentInt)
        views.setInt(R.id.widget_btn_prev, "setColorFilter", textPrimaryInt)
        views.setInt(R.id.widget_btn_next, "setColorFilter", textPrimaryInt)

        views.setOnClickPendingIntent(
            R.id.widget_btn_play_pause,
            commandPendingIntent(context, CMD_PLAY_PAUSE, 101)
        )
        views.setOnClickPendingIntent(
            R.id.widget_btn_prev,
            commandPendingIntent(context, CMD_PREV, 102)
        )
        views.setOnClickPendingIntent(
            R.id.widget_btn_next,
            commandPendingIntent(context, CMD_NEXT, 103)
        )

        // Open full app on root tap
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_player", true)
        }
        val openPending = PendingIntent.getActivity(
            context,
            200,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, openPending)

        return views
    }

    private fun commandPendingIntent(context: Context, cmd: String, reqCode: Int): PendingIntent {
        val intent = Intent(context, SonaraCompactWidgetProvider::class.java).apply {
            action = ACTION_WIDGET_COMMAND
            putExtra(EXTRA_COMMAND, cmd)
        }
        return PendingIntent.getBroadcast(
            context,
            reqCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun renderWidgetBackgroundBitmap(
        width: Int,
        height: Int,
        fillColor: Int,
        borderColor: Int,
        cornerRadiusPx: Float,
        isGlass: Boolean
    ): Bitmap {
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val rect = RectF(2f, 2f, width - 2f, height - 2f)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fillColor
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, paint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isGlass) ColorUtils.setAlphaComponent(0xFFFFFFFF.toInt(), 90) else borderColor
            style = Paint.Style.STROKE
            strokeWidth = if (isGlass) 3f else 2f
        }
        canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, borderPaint)
        return bmp
    }

    private fun loadRoundedArtworkBitmap(
        context: Context,
        uriStr: String?,
        sizePx: Int,
        radiusPx: Float
    ): Bitmap? {
        if (uriStr.isNullOrBlank()) return null
        return runCatching {
            val uri = Uri.parse(uriStr)
            val raw = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: return null

            val scaled = Bitmap.createScaledBitmap(raw, sizePx, sizePx, true)
            if (raw != scaled) raw.recycle()

            val output = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val rect = RectF(0f, 0f, sizePx.toFloat(), sizePx.toFloat())
            canvas.drawRoundRect(rect, radiusPx, radiusPx, paint)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(scaled, 0f, 0f, paint)
            scaled.recycle()
            output
        }.getOrNull()
    }

    private fun formatTime(ms: Long): String {
        val totalSec = (ms / 1000L).coerceAtLeast(0L)
        val min = totalSec / 60L
        val sec = totalSec % 60L
        return String.format(java.util.Locale.US, "%d:%02d", min, sec)
    }
}
