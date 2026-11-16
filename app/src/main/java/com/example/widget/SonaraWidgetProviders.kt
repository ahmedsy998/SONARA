package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.data.SonaraPreferencesRepository
import com.example.player.SonaraPlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class BaseSonaraWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val prefs = SonaraPreferencesRepository(context).getWidgetCustomizationsSnapshot()
            WidgetUpdater.updateAllWidgets(context, WidgetUpdater.lastSnapshot, prefs)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == WidgetUpdater.ACTION_WIDGET_COMMAND) {
            val cmd = intent.getStringExtra(WidgetUpdater.EXTRA_COMMAND) ?: return
            val serviceIntent = Intent(context, SonaraPlaybackService::class.java).apply {
                action = SonaraPlaybackService.ACTION_EXTERNAL_COMMAND
                putExtra(WidgetUpdater.EXTRA_COMMAND, cmd)
            }
            runCatching {
                ContextCompat.startForegroundService(context, serviceIntent)
            }.onFailure {
                context.startService(serviceIntent)
            }
        }
    }
}

class SonaraCompactWidgetProvider : BaseSonaraWidgetProvider()
class SonaraGlassWidgetProvider : BaseSonaraWidgetProvider()
class SonaraLargeWidgetProvider : BaseSonaraWidgetProvider()
class SonaraMinimalWidgetProvider : BaseSonaraWidgetProvider()
