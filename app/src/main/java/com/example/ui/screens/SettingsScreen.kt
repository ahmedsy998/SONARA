package com.example.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.PlaybackPreferences
import com.example.model.AppLanguage
import com.example.model.EqualizerConfig
import com.example.model.EqualizerPreset
import com.example.model.SonaraThemeId
import com.example.model.SonaraThemeSpec
import com.example.model.VisualizerStyle
import com.example.model.WidgetCustomizations
import com.example.theme.SonaraThemedCardSurface
import com.example.theme.horizontalScreenPadding
import com.example.ui.components.ColorPickerDialog
import com.example.ui.util.tr
import com.example.widget.SonaraCompactWidgetProvider
import com.example.widget.SonaraGlassWidgetProvider
import com.example.widget.SonaraLargeWidgetProvider
import com.example.widget.SonaraMinimalWidgetProvider

private enum class WidgetColorTarget { BG, TEXT, ACCENT }

@Composable
fun SettingsScreen(
    equalizerConfig: EqualizerConfig,
    widgetCustomizations: WidgetCustomizations,
    playbackPrefs: PlaybackPreferences,
    spec: SonaraThemeSpec,
    isArabic: Boolean,
    openEqualizerInitially: Boolean = false,
    onOpenThemeStudio: () -> Unit,
    onUpdateEqualizer: (EqualizerConfig) -> Unit,
    onSelectEqPreset: (EqualizerPreset) -> Unit,
    onUpdateWidgetCustomizations: (WidgetCustomizations) -> Unit,
    onSetPauseOnDisconnect: (Boolean) -> Unit,
    onSetVisualizerEnabled: (Boolean) -> Unit,
    onSetVisualizerStyle: (VisualizerStyle) -> Unit,
    onToggleLiveVisualizer: (Boolean) -> Unit,
    onSetLanguage: (AppLanguage) -> Unit,
    onRescanLibrary: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val hPad = spec.horizontalScreenPadding()
    var widgetColorTarget by remember { mutableStateOf<WidgetColorTarget?>(null) }
    val bandLabels = listOf("31", "62", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = hPad, end = hPad, top = 12.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = tr(isArabic, "Settings & Studio", "الإعدادات والاستوديو"),
                color = spec.textPrimary,
                style = MaterialTheme.typography.headlineLarge
            )
        }

        // Theme Studio Entry Card
        item {
            SonaraThemedCardSurface(
                spec = spec,
                elevated = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenThemeStudio)
                    .testTag("settings_theme_studio_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Filled.Palette, contentDescription = null, tint = spec.primary, modifier = Modifier.size(32.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tr(isArabic, "Theme Customization Studio", "استوديو تخصيص الثيمات"), color = spec.textPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${tr(isArabic, spec.id.displayNameEn, spec.id.displayNameAr)} • 15 ${tr(isArabic, "Themes", "ثيمات")}",
                            color = spec.textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Button(
                        onClick = onOpenThemeStudio,
                        colors = ButtonDefaults.buttonColors(containerColor = spec.primary)
                    ) {
                        Text(tr(isArabic, "Open", "فتح"), color = Color.Black)
                    }
                }
            }
        }

        // 10-Band Equalizer & Audio Effects
        item {
            SonaraThemedCardSurface(
                spec = spec,
                elevated = openEqualizerInitially,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("equalizer_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Equalizer, contentDescription = null, tint = spec.primary)
                            Text(tr(isArabic, "10-Band Equalizer & DSP", "معادل الصوت الاحترافي (10 نطاقات)"), color = spec.textPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Switch(
                            checked = equalizerConfig.enabled,
                            onCheckedChange = { onUpdateEqualizer(equalizerConfig.copy(enabled = it)) }
                        )
                    }

                    // 11 EQ Presets
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(EqualizerPreset.entries) { preset ->
                            FilterChip(
                                selected = equalizerConfig.presetName == preset.name,
                                onClick = { onSelectEqPreset(preset) },
                                label = { Text(tr(isArabic, preset.labelEn, preset.labelAr)) }
                            )
                        }
                    }

                    // 10-Band Sliders
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        bandLabels.forEachIndexed { idx, freq ->
                            val dbVal = equalizerConfig.bandLevelsDb.getOrElse(idx) { 0 }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${freq}Hz",
                                    color = spec.textSecondary,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.width(46.dp)
                                )
                                Slider(
                                    value = dbVal.toFloat(),
                                    onValueChange = { newVal ->
                                        val updated = equalizerConfig.bandLevelsDb.toMutableList()
                                        while (updated.size < 10) updated.add(0)
                                        updated[idx] = newVal.toInt()
                                        onUpdateEqualizer(
                                            equalizerConfig.copy(
                                                presetName = EqualizerPreset.CUSTOM.name,
                                                bandLevelsDb = updated
                                            )
                                        )
                                    },
                                    valueRange = -12f..12f,
                                    enabled = equalizerConfig.enabled,
                                    colors = SliderDefaults.colors(thumbColor = spec.primary, activeTrackColor = spec.primary),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${if (dbVal > 0) "+" else ""}${dbVal}dB",
                                    color = spec.primary,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.width(42.dp)
                                )
                            }
                        }
                    }

                    // Bass Boost & Virtualizer
                    Text("${tr(isArabic, "Bass Boost", "تعزيز الجهير")}: ${equalizerConfig.bassBoostStrength / 10}%", color = spec.textSecondary)
                    Slider(
                        value = equalizerConfig.bassBoostStrength.toFloat(),
                        onValueChange = { onUpdateEqualizer(equalizerConfig.copy(bassBoostStrength = it.toInt())) },
                        valueRange = 0f..1000f,
                        enabled = equalizerConfig.enabled
                    )

                    Text("${tr(isArabic, "Spatial Virtualizer", "المحيط الصوتي")}: ${equalizerConfig.virtualizerStrength / 10}%", color = spec.textSecondary)
                    Slider(
                        value = equalizerConfig.virtualizerStrength.toFloat(),
                        onValueChange = { onUpdateEqualizer(equalizerConfig.copy(virtualizerStrength = it.toInt())) },
                        valueRange = 0f..1000f,
                        enabled = equalizerConfig.enabled
                    )
                }
            }
        }

        // Home Screen Widget Customization Studio
        item {
            SonaraThemedCardSurface(spec = spec, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Widgets, contentDescription = null, tint = spec.primary)
                        Text(tr(isArabic, "Home Screen Widgets Studio", "استوديو ويدجت الشاشة الرئيسية"), color = spec.textPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    // Live preview of widget appearance
                    val wBg = Color(widgetCustomizations.backgroundColorHex).copy(alpha = widgetCustomizations.transparency)
                    val wText = Color(widgetCustomizations.textColorHex)
                    val wAccent = Color(widgetCustomizations.accentColorHex)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(widgetCustomizations.cornerRadiusDp.dp))
                            .background(wBg)
                            .border(1.dp, wAccent.copy(alpha = 0.6f), RoundedCornerShape(widgetCustomizations.cornerRadiusDp.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("SONARA WIDGET", color = wAccent, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text("Now Playing Track", color = wText, fontWeight = FontWeight.Bold)
                            Text("Artist Name", color = wText.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
                        }
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = wAccent, modifier = Modifier.size(34.dp))
                    }

                    Text(tr(isArabic, "Widget Theme Style", "نمط الويدجت"), color = spec.textSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(SonaraThemeId.entries) { tid ->
                            FilterChip(
                                selected = widgetCustomizations.widgetThemeId == tid.name,
                                onClick = { onUpdateWidgetCustomizations(widgetCustomizations.copy(widgetThemeId = tid.name)) },
                                label = { Text(tr(isArabic, tid.displayNameEn, tid.displayNameAr)) }
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        WidgetColorChip(tr(isArabic, "Bg", "الخلفية"), Color(widgetCustomizations.backgroundColorHex), spec) { widgetColorTarget = WidgetColorTarget.BG }
                        WidgetColorChip(tr(isArabic, "Text", "النص"), Color(widgetCustomizations.textColorHex), spec) { widgetColorTarget = WidgetColorTarget.TEXT }
                        WidgetColorChip(tr(isArabic, "Accent", "التمييز"), Color(widgetCustomizations.accentColorHex), spec) { widgetColorTarget = WidgetColorTarget.ACCENT }
                    }

                    Text("${tr(isArabic, "Widget Opacity", "شفافية الويدجت")}: ${(widgetCustomizations.transparency * 100).toInt()}%", color = spec.textSecondary)
                    Slider(
                        value = widgetCustomizations.transparency,
                        onValueChange = { onUpdateWidgetCustomizations(widgetCustomizations.copy(transparency = it)) },
                        valueRange = 0.25f..1f
                    )

                    Text("${tr(isArabic, "Widget Corner Radius", "استدارة زوايا الويدجت")}: ${widgetCustomizations.cornerRadiusDp.toInt()}dp", color = spec.textSecondary)
                    Slider(
                        value = widgetCustomizations.cornerRadiusDp,
                        onValueChange = { onUpdateWidgetCustomizations(widgetCustomizations.copy(cornerRadiusDp = it)) },
                        valueRange = 4f..32f
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(tr(isArabic, "Show Album Artwork", "إظهار غلاف الألبوم"), color = spec.textPrimary)
                        Switch(
                            checked = widgetCustomizations.showArtwork,
                            onCheckedChange = { onUpdateWidgetCustomizations(widgetCustomizations.copy(showArtwork = it)) }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(tr(isArabic, "Show Progress Bar", "إظهار شريط التقدم"), color = spec.textPrimary)
                        Switch(
                            checked = widgetCustomizations.showProgress,
                            onCheckedChange = { onUpdateWidgetCustomizations(widgetCustomizations.copy(showProgress = it)) }
                        )
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val awm = AppWidgetManager.getInstance(context)
                        if (awm.isRequestPinAppWidgetSupported) {
                            Text(tr(isArabic, "Pin Real Home Screen Widget:", "تثبيت ويدجت على الشاشة الرئيسية:"), color = spec.textSecondary)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val providers = listOf(
                                    "Compact 4x1" to SonaraCompactWidgetProvider::class.java,
                                    "Glass 4x2" to SonaraGlassWidgetProvider::class.java,
                                    "Large 4x3" to SonaraLargeWidgetProvider::class.java,
                                    "Minimal 2x2" to SonaraMinimalWidgetProvider::class.java
                                )
                                items(providers) { (label, cls) ->
                                    OutlinedButton(onClick = {
                                        awm.requestPinAppWidget(ComponentName(context, cls), null, null)
                                    }) {
                                        Text(label, color = spec.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Playback, Visualizer & Language Settings
        item {
            SonaraThemedCardSurface(spec = spec, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Language, contentDescription = null, tint = spec.primary)
                        Text(tr(isArabic, "Language & Playback", "اللغة والتشغيل"), color = spec.textPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AppLanguage.entries.forEach { lang ->
                            FilterChip(
                                selected = playbackPrefs.language == lang,
                                onClick = { onSetLanguage(lang) },
                                label = { Text(lang.label) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(tr(isArabic, "Pause when headphones unplug", "إيقاف عند فصل السماعات"), color = spec.textPrimary, modifier = Modifier.weight(1f))
                        Switch(checked = playbackPrefs.pauseOnHeadphoneDisconnect, onCheckedChange = onSetPauseOnDisconnect)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(tr(isArabic, "Show Audio Visualizer", "إظهار محلل الصوت"), color = spec.textPrimary, modifier = Modifier.weight(1f))
                        Switch(checked = playbackPrefs.visualizerEnabled, onCheckedChange = onSetVisualizerEnabled)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(tr(isArabic, "Live Audio FFT Capture", "التقاط طيف الصوت الحي"), color = spec.textPrimary, modifier = Modifier.weight(1f))
                        Switch(checked = playbackPrefs.visualizerLiveAudioEnabled, onCheckedChange = onToggleLiveVisualizer)
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(VisualizerStyle.entries) { vs ->
                            FilterChip(
                                selected = playbackPrefs.visualizerStyle == vs,
                                onClick = { onSetVisualizerStyle(vs) },
                                label = { Text(tr(isArabic, vs.labelEn, vs.labelAr)) }
                            )
                        }
                    }

                    OutlinedButton(onClick = {
                        onRescanLibrary()
                        onShowMessage(tr(isArabic, "Scanning local audio library...", "جاري فحص المكتبة الصوتية..."))
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(tr(isArabic, "Rescan Local Audio Library", "إعادة فحص المكتبة الصوتية"), color = spec.primary)
                    }
                }
            }
        }

        // System & Lock Screen Integration Info (transparent, accurate technical note)
        item {
            SonaraThemedCardSurface(spec = spec, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Security, contentDescription = null, tint = spec.primary)
                        Text(tr(isArabic, "System & Lock-Screen Notes", "ملاحظات النظام وشاشة القفل"), color = spec.textPrimary, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = tr(
                            isArabic,
                            "SONARA uses AndroidX Media3 MediaLibraryService for background playback, Bluetooth AVRCP controls, Android Auto browsing, and system lock-screen media controls. On Android 10–14+ (including HyperOS / MIUI), lock-screen card layout and button placement are controlled by the system UI.",
                            "SONARA uses AndroidX Media3 MediaLibraryService for genuine background playback, Bluetooth headset controls, Android Auto browsing, and system lock-screen media notifications. On Android 10–14+ (including Xiaomi HyperOS), lock-screen media card styling and button placement are managed by Android SystemUI and cannot be overridden by third-party apps."
                        ),
                        color = spec.textSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    widgetColorTarget?.let { target ->
        val initHex = when (target) {
            WidgetColorTarget.BG -> widgetCustomizations.backgroundColorHex
            WidgetColorTarget.TEXT -> widgetCustomizations.textColorHex
            WidgetColorTarget.ACCENT -> widgetCustomizations.accentColorHex
        }
        ColorPickerDialog(
            title = "Widget ${target.name}",
            initialHex = initHex,
            isArabic = isArabic,
            onDismiss = { widgetColorTarget = null },
            onColorSelected = { hex ->
                val updated = when (target) {
                    WidgetColorTarget.BG -> widgetCustomizations.copy(backgroundColorHex = hex)
                    WidgetColorTarget.TEXT -> widgetCustomizations.copy(textColorHex = hex)
                    WidgetColorTarget.ACCENT -> widgetCustomizations.copy(accentColorHex = hex)
                }
                onUpdateWidgetCustomizations(updated)
            }
        )
    }
}

@Composable
private fun WidgetColorChip(
    label: String,
    color: Color,
    spec: SonaraThemeSpec,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, spec.borderColor, CircleShape)
        )
        Text(label, color = spec.textPrimary, style = MaterialTheme.typography.labelLarge)
    }
}
