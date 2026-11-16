package com.example.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.model.SonaraThemeId
import com.example.model.SonaraThemeSpec

val LocalSonaraTheme = staticCompositionLocalOf {
    SonaraThemeCatalog.defaultSpecFor(SonaraThemeId.GLASSMORPHISM_PRO, isDark = true)
}

@Composable
fun SonaraTheme(
    spec: SonaraThemeSpec,
    isArabic: Boolean,
    useSystemDynamicMaterialYou: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val baseScheme = if (spec.id == SonaraThemeId.GOOGLE_PIXEL_MATERIAL_YOU &&
        useSystemDynamicMaterialYou &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    ) {
        if (spec.isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else if (spec.isDark) {
        darkColorScheme(
            primary = spec.primary,
            onPrimary = Color.Black,
            secondary = spec.secondary,
            onSecondary = Color.White,
            tertiary = spec.accent,
            background = spec.background,
            onBackground = spec.textPrimary,
            surface = spec.surface,
            onSurface = spec.textPrimary,
            surfaceVariant = spec.surfaceElevated,
            onSurfaceVariant = spec.textSecondary,
            outline = spec.borderColor
        )
    } else {
        lightColorScheme(
            primary = spec.primary,
            onPrimary = Color.White,
            secondary = spec.secondary,
            onSecondary = Color.White,
            tertiary = spec.accent,
            background = spec.background,
            onBackground = spec.textPrimary,
            surface = spec.surface,
            onSurface = spec.textPrimary,
            surfaceVariant = spec.surfaceElevated,
            onSurfaceVariant = spec.textSecondary,
            outline = spec.borderColor
        )
    }

    val (displayFamily, bodyFamily) = SonaraFonts.resolveFamilies(
        themeId = spec.id,
        fontChoice = spec.fontChoice,
        preferredArabicFont = spec.preferredArabicFont,
        isArabic = isArabic
    )
    val typography = SonaraFonts.buildTypography(displayFamily, bodyFamily, spec.density.scale)

    val r = spec.cornerRadiusDp.dp
    val shapes = Shapes(
        small = RoundedCornerShape((r * 0.5f).coerceAtLeast(2.dp)),
        medium = RoundedCornerShape(r),
        large = RoundedCornerShape((r * 1.25f).coerceAtMost(32.dp))
    )

    CompositionLocalProvider(LocalSonaraTheme provides spec) {
        MaterialTheme(
            colorScheme = baseScheme,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}
