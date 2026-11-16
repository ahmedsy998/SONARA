package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CustomThemePresetEntity
import com.example.model.AppearanceMode
import com.example.model.FontChoice
import com.example.model.SonaraThemeId
import com.example.model.SonaraThemeSpec
import com.example.model.ThemeCustomizations
import com.example.model.UiDensity
import com.example.theme.SonaraThemeCatalog
import com.example.theme.SonaraThemedCardSurface
import com.example.theme.horizontalScreenPadding
import com.example.ui.components.ColorPickerDialog
import com.example.ui.util.tr

private enum class ColorTarget {
    PRIMARY, SECONDARY, ACCENT, BACKGROUND, SURFACE, TEXT, GRADIENT_START, GRADIENT_END
}

@Composable
fun ThemeStudioScreen(
    savedCustomizations: ThemeCustomizations,
    savedPresets: List<CustomThemePresetEntity>,
    activeSpec: SonaraThemeSpec,
    isArabic: Boolean,
    onPreviewChange: (ThemeCustomizations?) -> Unit,
    onApplyTheme: (ThemeCustomizations) -> Unit,
    onRestoreDefault: () -> Unit,
    onSavePreset: (String, ThemeCustomizations) -> Unit,
    onApplyPreset: (CustomThemePresetEntity) -> Unit,
    onDeletePreset: (Long) -> Unit,
    onBack: () -> Unit
) {
    var draft by remember(savedCustomizations) { mutableStateOf(savedCustomizations) }
    var colorPickerTarget by remember { mutableStateOf<ColorTarget?>(null) }
    var showSavePresetDialog by remember { mutableStateOf(false) }

    fun updateDraft(updated: ThemeCustomizations) {
        draft = updated
        onPreviewChange(updated)
    }

    DisposableEffect(Unit) {
        onDispose { onPreviewChange(null) }
    }

    BackHandler {
        onPreviewChange(null)
        onBack()
    }

    val currentThemeId = runCatching { SonaraThemeId.valueOf(draft.themeId) }
        .getOrDefault(SonaraThemeId.GLASSMORPHISM_PRO)
    val hPad = activeSpec.horizontalScreenPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("theme_studio_screen")
    ) {
        // Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = hPad, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    onPreviewChange(null)
                    onBack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr(isArabic, "Back", "رجوع"), tint = activeSpec.textPrimary)
                }
                Text(
                    text = tr(isArabic, "Theme Studio", "استوديو الثيمات"),
                    color = activeSpec.textPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Row {
                IconButton(onClick = { showSavePresetDialog = true }) {
                    Icon(Icons.Filled.BookmarkAdd, contentDescription = tr(isArabic, "Save Preset", "حفظ الإعداد"), tint = activeSpec.primary)
                }
                IconButton(onClick = onRestoreDefault) {
                    Icon(Icons.Filled.Restore, contentDescription = tr(isArabic, "Restore Default", "استعادة الافتراضي"), tint = activeSpec.textSecondary)
                }
                Button(
                    onClick = { onApplyTheme(draft) },
                    colors = ButtonDefaults.buttonColors(containerColor = activeSpec.primary),
                    modifier = Modifier.testTag("theme_studio_apply_btn")
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(tr(isArabic, "Apply", "تطبيق"), color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(start = hPad, end = hPad, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Preview Card
            item {
                SonaraThemedCardSurface(spec = activeSpec, elevated = true, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = tr(isArabic, "LIVE PREVIEW • ${currentThemeId.displayNameEn}", "معاينة حية • ${currentThemeId.displayNameAr}"),
                            color = activeSpec.primary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tr(isArabic, "Midnight Resonance", "صدى منتصف الليل"), color = activeSpec.textPrimary, style = MaterialTheme.typography.titleMedium)
                                Text(tr(isArabic, "SONARA Studio Engine", "محرك استوديو سونارا"), color = activeSpec.textSecondary, style = MaterialTheme.typography.bodyMedium)
                            }
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(activeSpec.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.Black)
                            }
                        }
                    }
                }
            }

            // 15 Predefined Themes
            item {
                Text(
                    text = tr(isArabic, "15 Visual Design Systems", "15 نظام تصميم بصري"),
                    color = activeSpec.textPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(SonaraThemeId.entries, key = { it.name }) { themeId ->
                        val preview = SonaraThemeCatalog.defaultSpecFor(themeId, activeSpec.isDark)
                        val selected = themeId == currentThemeId
                        Column(
                            modifier = Modifier
                                .width(154.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(preview.surface)
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) activeSpec.primary else preview.borderColor.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    updateDraft(
                                        draft.copy(
                                            themeId = themeId.name,
                                            useCustomColors = false,
                                            useCustomGradient = false,
                                            cornerRadiusDp = preview.cornerRadiusDp,
                                            cardTransparency = preview.cardAlpha,
                                            blurIntensity = preview.blurIntensity,
                                            uiDensity = preview.density.name
                                        )
                                    )
                                }
                                .padding(12.dp)
                                .testTag("studio_theme_${themeId.name.lowercase()}"),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(Modifier.size(14.dp).clip(CircleShape).background(preview.primary))
                                Box(Modifier.size(14.dp).clip(CircleShape).background(preview.secondary))
                                Box(Modifier.size(14.dp).clip(CircleShape).background(preview.accent))
                            }
                            Text(
                                text = tr(isArabic, themeId.displayNameEn, themeId.displayNameAr),
                                color = preview.textPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = tr(isArabic, themeId.subtitleEn, themeId.subtitleAr),
                                color = preview.textSecondary,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Curated Color Palettes
            item {
                val palettes = SonaraThemeCatalog.curatedPalettesFor(currentThemeId)
                Text(
                    text = tr(isArabic, "Curated Palettes", "لوحات الألوان المختارة"),
                    color = activeSpec.textPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(palettes, key = { it.id }) { p ->
                        SonaraThemedCardSurface(
                            spec = activeSpec,
                            modifier = Modifier.clickable {
                                updateDraft(
                                    draft.copy(
                                        useCustomColors = true,
                                        customPrimaryHex = p.primaryHex,
                                        customSecondaryHex = p.secondaryHex,
                                        customAccentHex = p.accentHex,
                                        customBackgroundHex = p.backgroundHex,
                                        customSurfaceHex = p.surfaceHex,
                                        customTextHex = p.textHex
                                    )
                                )
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(Modifier.size(18.dp).clip(CircleShape).background(Color(p.primaryHex)))
                                Box(Modifier.size(18.dp).clip(CircleShape).background(Color(p.secondaryHex)))
                                Text(tr(isArabic, p.name, p.nameAr), color = activeSpec.textPrimary, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            // Appearance Mode & Dynamic Album Art Colors
            item {
                SonaraThemedCardSurface(spec = activeSpec, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(tr(isArabic, "Appearance & Dynamic Colors", "المظهر والألوان التفاعلية"), color = activeSpec.textPrimary, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AppearanceMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = draft.appearanceMode == mode.name,
                                    onClick = { updateDraft(draft.copy(appearanceMode = mode.name)) },
                                    label = { Text(tr(isArabic, mode.labelEn, mode.labelAr)) }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tr(isArabic, "Extract colors from Album Art", "استخراج الألوان من غلاف الألبوم"),
                                color = activeSpec.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = draft.dynamicAlbumArtColors,
                                onCheckedChange = { updateDraft(draft.copy(dynamicAlbumArtColors = it)) }
                            )
                        }
                    }
                }
            }

            // Custom Colors & Gradient Editor
            item {
                SonaraThemedCardSurface(spec = activeSpec, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tr(isArabic, "Custom Color Palette", "تخصيص الألوان بالكامل"), color = activeSpec.textPrimary, fontWeight = FontWeight.Bold)
                            Switch(
                                checked = draft.useCustomColors,
                                onCheckedChange = { updateDraft(draft.copy(useCustomColors = it)) }
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            ColorSwatchButton(tr(isArabic, "Primary", "أساسي"), Color(draft.customPrimaryHex), activeSpec) { colorPickerTarget = ColorTarget.PRIMARY }
                            ColorSwatchButton(tr(isArabic, "Secondary", "ثانوي"), Color(draft.customSecondaryHex), activeSpec) { colorPickerTarget = ColorTarget.SECONDARY }
                            ColorSwatchButton(tr(isArabic, "Accent", "تمييز"), Color(draft.customAccentHex), activeSpec) { colorPickerTarget = ColorTarget.ACCENT }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            ColorSwatchButton(tr(isArabic, "Background", "خلفية"), Color(draft.customBackgroundHex), activeSpec) { colorPickerTarget = ColorTarget.BACKGROUND }
                            ColorSwatchButton(tr(isArabic, "Surface", "بطاقة"), Color(draft.customSurfaceHex), activeSpec) { colorPickerTarget = ColorTarget.SURFACE }
                            ColorSwatchButton(tr(isArabic, "Text", "نص"), Color(draft.customTextHex), activeSpec) { colorPickerTarget = ColorTarget.TEXT }
                        }

                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tr(isArabic, "Custom Background Gradient", "محرر التدرج اللوني"), color = activeSpec.textPrimary, fontWeight = FontWeight.Bold)
                            Switch(
                                checked = draft.useCustomGradient,
                                onCheckedChange = { updateDraft(draft.copy(useCustomGradient = it)) }
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ColorSwatchButton(tr(isArabic, "Start", "بداية"), Color(draft.gradientStartHex), activeSpec) { colorPickerTarget = ColorTarget.GRADIENT_START }
                            ColorSwatchButton(tr(isArabic, "End", "نهاية"), Color(draft.gradientEndHex), activeSpec) { colorPickerTarget = ColorTarget.GRADIENT_END }
                        }
                        Text("${tr(isArabic, "Gradient Angle", "زاوية التدرج")}: ${draft.gradientAngleDeg.toInt()}°", color = activeSpec.textSecondary)
                        Slider(
                            value = draft.gradientAngleDeg,
                            onValueChange = { updateDraft(draft.copy(gradientAngleDeg = it, useCustomGradient = true)) },
                            valueRange = 0f..360f,
                            colors = SliderDefaults.colors(thumbColor = activeSpec.primary, activeTrackColor = activeSpec.primary)
                        )
                    }
                }
            }

            // Geometry, Transparency, Blur, Density
            item {
                SonaraThemedCardSurface(spec = activeSpec, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(tr(isArabic, "Surface, Corners & Density", "الشفافية والزوايا والكثافة"), color = activeSpec.textPrimary, fontWeight = FontWeight.Bold)

                        Text("${tr(isArabic, "Card Transparency", "شفافية البطاقات")}: ${(draft.cardTransparency * 100).toInt()}%", color = activeSpec.textSecondary)
                        Slider(
                            value = draft.cardTransparency,
                            onValueChange = { updateDraft(draft.copy(cardTransparency = it)) },
                            valueRange = 0.25f..1f
                        )

                        Text("${tr(isArabic, "Blur / Glow Intensity", "شدة الضبابية والتوهج")}: ${(draft.blurIntensity * 100).toInt()}%", color = activeSpec.textSecondary)
                        Slider(
                            value = draft.blurIntensity,
                            onValueChange = { updateDraft(draft.copy(blurIntensity = it)) },
                            valueRange = 0f..1f
                        )

                        Text("${tr(isArabic, "Corner Radius", "استدارة الزوايا")}: ${draft.cornerRadiusDp.toInt()} dp", color = activeSpec.textSecondary)
                        Slider(
                            value = draft.cornerRadiusDp,
                            onValueChange = { updateDraft(draft.copy(cornerRadiusDp = it)) },
                            valueRange = 0f..32f
                        )

                        Text(tr(isArabic, "UI Density", "كثافة الواجهة"), color = activeSpec.textSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            UiDensity.entries.forEach { d ->
                                FilterChip(
                                    selected = draft.uiDensity == d.name,
                                    onClick = { updateDraft(draft.copy(uiDensity = d.name)) },
                                    label = { Text(tr(isArabic, d.labelEn, d.labelAr)) }
                                )
                            }
                        }
                    }
                }
            }

            // Typography Selector
            item {
                SonaraThemedCardSurface(spec = activeSpec, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(tr(isArabic, "Custom Font Family", "اختيار الخط"), color = activeSpec.textPrimary, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(FontChoice.entries) { font ->
                                FilterChip(
                                    selected = draft.fontChoice == font.name,
                                    onClick = { updateDraft(draft.copy(fontChoice = font.name)) },
                                    label = { Text(tr(isArabic, font.labelEn, font.labelAr)) }
                                )
                            }
                        }
                    }
                }
            }

            // Saved Personal Theme Presets
            if (savedPresets.isNotEmpty()) {
                item {
                    Text(
                        text = tr(isArabic, "Saved Personal Presets", "الثيمات المحفوظة"),
                        color = activeSpec.textPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        savedPresets.forEach { preset ->
                            SonaraThemedCardSurface(
                                spec = activeSpec,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onApplyPreset(preset) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val baseTheme = runCatching { SonaraThemeId.valueOf(preset.baseThemeId) }.getOrNull()
                                    Column {
                                        Text(preset.name, color = activeSpec.textPrimary, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = baseTheme?.let { tr(isArabic, it.displayNameEn, it.displayNameAr) } ?: preset.baseThemeId,
                                            color = activeSpec.textSecondary,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    IconButton(onClick = { onDeletePreset(preset.id) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = tr(isArabic, "Delete preset", "حذف الثيم المحفوظ"), tint = activeSpec.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    colorPickerTarget?.let { target ->
        val initHex = when (target) {
            ColorTarget.PRIMARY -> draft.customPrimaryHex
            ColorTarget.SECONDARY -> draft.customSecondaryHex
            ColorTarget.ACCENT -> draft.customAccentHex
            ColorTarget.BACKGROUND -> draft.customBackgroundHex
            ColorTarget.SURFACE -> draft.customSurfaceHex
            ColorTarget.TEXT -> draft.customTextHex
            ColorTarget.GRADIENT_START -> draft.gradientStartHex
            ColorTarget.GRADIENT_END -> draft.gradientEndHex
        }
        val targetTitle = when (target) {
            ColorTarget.PRIMARY -> tr(isArabic, "Primary Color", "اللون الأساسي")
            ColorTarget.SECONDARY -> tr(isArabic, "Secondary Color", "اللون الثانوي")
            ColorTarget.ACCENT -> tr(isArabic, "Accent Color", "لون التمييز")
            ColorTarget.BACKGROUND -> tr(isArabic, "Background Color", "لون الخلفية")
            ColorTarget.SURFACE -> tr(isArabic, "Surface Color", "لون البطاقة")
            ColorTarget.TEXT -> tr(isArabic, "Text Color", "لون النص")
            ColorTarget.GRADIENT_START -> tr(isArabic, "Gradient Start Color", "لون بداية التدرج")
            ColorTarget.GRADIENT_END -> tr(isArabic, "Gradient End Color", "لون نهاية التدرج")
        }
        ColorPickerDialog(
            title = targetTitle,
            initialHex = initHex,
            isArabic = isArabic,
            onDismiss = { colorPickerTarget = null },
            onColorSelected = { selectedHex ->
                val updated = when (target) {
                    ColorTarget.PRIMARY -> draft.copy(useCustomColors = true, customPrimaryHex = selectedHex)
                    ColorTarget.SECONDARY -> draft.copy(useCustomColors = true, customSecondaryHex = selectedHex)
                    ColorTarget.ACCENT -> draft.copy(useCustomColors = true, customAccentHex = selectedHex)
                    ColorTarget.BACKGROUND -> draft.copy(useCustomColors = true, customBackgroundHex = selectedHex)
                    ColorTarget.SURFACE -> draft.copy(useCustomColors = true, customSurfaceHex = selectedHex)
                    ColorTarget.TEXT -> draft.copy(useCustomColors = true, customTextHex = selectedHex)
                    ColorTarget.GRADIENT_START -> draft.copy(useCustomGradient = true, gradientStartHex = selectedHex)
                    ColorTarget.GRADIENT_END -> draft.copy(useCustomGradient = true, gradientEndHex = selectedHex)
                }
                updateDraft(updated)
            }
        )
    }

    if (showSavePresetDialog) {
        var presetName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text(tr(isArabic, "Save Personal Theme Preset", "حفظ الثيم المخصص")) },
            text = {
                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    label = { Text(tr(isArabic, "Preset name", "اسم الثيم")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (presetName.isNotBlank()) {
                        onSavePreset(presetName, draft)
                        showSavePresetDialog = false
                    }
                }) {
                    Text(tr(isArabic, "Save", "حفظ"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text(tr(isArabic, "Cancel", "إلغاء"))
                }
            }
        )
    }
}

@Composable
private fun ColorSwatchButton(
    label: String,
    color: Color,
    spec: SonaraThemeSpec,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, spec.borderColor, CircleShape)
        )
        Text(label, color = spec.textPrimary, style = MaterialTheme.typography.labelLarge)
    }
}
