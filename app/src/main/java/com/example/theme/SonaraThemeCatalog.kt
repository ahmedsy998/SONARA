package com.example.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.example.data.DynamicArtColors
import com.example.model.AppearanceMode
import com.example.model.ArtworkShapeStyle
import com.example.model.BackgroundTreatment
import com.example.model.CardStyle
import com.example.model.FontChoice
import com.example.model.NavStyle
import com.example.model.PlayerLayoutStyle
import com.example.model.SonaraThemeId
import com.example.model.SonaraThemeSpec
import com.example.model.ThemeCustomizations
import com.example.model.ThemePalettePreset
import com.example.model.UiDensity

object SonaraThemeCatalog {

    fun defaultSpecFor(id: SonaraThemeId, isDark: Boolean): SonaraThemeSpec {
        return when (id) {
            SonaraThemeId.GLASSMORPHISM_PRO -> SonaraThemeSpec(
                id = id,
                isDark = isDark,
                primary = Color(0xFF38BDF8),
                secondary = Color(0xFF8B5CF6),
                accent = Color(0xFF22D3EE),
                background = if (isDark) Color(0xFF070B19) else Color(0xFFEAF2FF),
                backgroundSecondary = if (isDark) Color(0xFF131D3B) else Color(0xFFDCE8FF),
                surface = if (isDark) Color(0xFF16203C) else Color(0xFFFFFFFF),
                surfaceElevated = if (isDark) Color(0xFF1F2D52) else Color(0xFFF3F7FF),
                textPrimary = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
                textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                borderColor = if (isDark) Color(0x55FFFFFF) else Color(0x8838BDF8),
                gradientStart = if (isDark) Color(0xFF0A1026) else Color(0xFFE0ECFF),
                gradientEnd = if (isDark) Color(0xFF1A103D) else Color(0xFFF3E8FF),
                gradientAngleDeg = 135f,
                useGradientBackground = true,
                cardAlpha = if (isDark) 0.42f else 0.65f,
                blurIntensity = 0.78f,
                cornerRadiusDp = 24f,
                density = UiDensity.COMFORTABLE,
                fontChoice = FontChoice.THEME_DEFAULT,
                navStyle = NavStyle.FLOATING_GLASS_PILL,
                playerLayoutStyle = PlayerLayoutStyle.GLASS_FLOATING_DOCK,
                artworkShapeStyle = ArtworkShapeStyle.ROUNDED_SOFT,
                cardStyle = CardStyle.TRANSLUCENT_GLASS,
                backgroundTreatment = BackgroundTreatment.RADIAL_GLOW_MESH
            )

            SonaraThemeId.SAMSUNG_ONE_UI -> SonaraThemeSpec(
                id = id,
                isDark = isDark,
                primary = Color(0xFF3B82F6),
                secondary = Color(0xFF60A5FA),
                accent = Color(0xFF2563EB),
                background = if (isDark) Color(0xFF010101) else Color(0xFFF2F4F7),
                backgroundSecondary = if (isDark) Color(0xFF0E1117) else Color(0xFFE5E9F0),
                surface = if (isDark) Color(0xFF171A21) else Color(0xFFFFFFFF),
                surfaceElevated = if (isDark) Color(0xFF212630) else Color(0xFFF8FAFC),
                textPrimary = if (isDark) Color(0xFFFAFAFA) else Color(0xFF111827),
                textSecondary = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                borderColor = if (isDark) Color(0xFF262B36) else Color(0xFFE2E8F0),
                gradientStart = if (isDark) Color(0xFF010101) else Color(0xFFF2F4F7),
                gradientEnd = if (isDark) Color(0xFF0D111A) else Color(0xFFE9EEF5),
                gradientAngleDeg = 180f,
                useGradientBackground = false,
                cardAlpha = 1f,
                blurIntensity = 0.25f,
                cornerRadiusDp = 26f,
                density = UiDensity.SPACIOUS,
                fontChoice = FontChoice.THEME_DEFAULT,
                navStyle = NavStyle.ONE_UI_ROUNDED_DOCK,
                playerLayoutStyle = PlayerLayoutStyle.ONE_UI_BOTTOM_ANCHORED,
                artworkShapeStyle = ArtworkShapeStyle.SQUIRCLE_LARGE,
                cardStyle = CardStyle.ONE_UI_PANEL,
                backgroundTreatment = BackgroundTreatment.CLEAN_TONAL
            )

            SonaraThemeId.GOOGLE_PIXEL_MATERIAL_YOU -> SonaraThemeSpec(
                id = id,
                isDark = isDark,
                primary = if (isDark) Color(0xFFD0BCFF) else Color(0xFF6750A4),
                secondary = if (isDark) Color(0xFFCCC2DC) else Color(0xFF625B71),
                accent = if (isDark) Color(0xFFA8C7FA) else Color(0xFF0B57D0),
                background = if (isDark) Color(0xFF141218) else Color(0xFFFEF7FF),
                backgroundSecondary = if (isDark) Color(0xFF1D1B20) else Color(0xFFF3EDF7),
                surface = if (isDark) Color(0xFF211F26) else Color(0xFFF7F2FA),
                surfaceElevated = if (isDark) Color(0xFF2B2930) else Color(0xFFECE6F0),
                textPrimary = if (isDark) Color(0xFFE6E0E9) else Color(0xFF1D1B20),
                textSecondary = if (isDark) Color(0xFFCAC4D0) else Color(0xFF49454F),
                borderColor = if (isDark) Color(0xFF49454F) else Color(0xFFCAC4D0),
                gradientStart = if (isDark) Color(0xFF141218) else Color(0xFFFEF7FF),
                gradientEnd = if (isDark) Color(0xFF1E1A2B) else Color(0xFFEADDFF),
                gradientAngleDeg = 140f,
                useGradientBackground = true,
                cardAlpha = 0.94f,
                blurIntensity = 0.2f,
                cornerRadiusDp = 28f,
                density = UiDensity.COMFORTABLE,
                fontChoice = FontChoice.THEME_DEFAULT,
                navStyle = NavStyle.MATERIAL_YOU_BAR,
                playerLayoutStyle = PlayerLayoutStyle.PIXEL_EXPRESSIVE_CARD,
                artworkShapeStyle = ArtworkShapeStyle.SCALLOPED_FLOWER,
                cardStyle = CardStyle.MATERIAL_TONAL,
                backgroundTreatment = BackgroundTreatment.DYNAMIC_PILL_CANVAS
            )

            SonaraThemeId.NOTHING_OS -> SonaraThemeSpec(
                id = id,
                isDark = isDark,
                primary = Color(0xFFD71921),
                secondary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF111111),
                accent = Color(0xFFD71921),
                background = if (isDark) Color(0xFF000000) else Color(0xFFF5F5F2),
                backgroundSecondary = if (isDark) Color(0xFF0D0D0D) else Color(0xFFE8E8E3),
                surface = if (isDark) Color(0xFF121212) else Color(0xFFFFFFFF),
                surfaceElevated = if (isDark) Color(0xFF1B1B1B) else Color(0xFFEDEDEA),
                textPrimary = if (isDark) Color(0xFFF5F5F5) else Color(0xFF0A0A0A),
                textSecondary = if (isDark) Color(0xFF8C8C8C) else Color(0xFF5A5A5A),
                borderColor = if (isDark) Color(0xFF333333) else Color(0xFFC4C4BF),
                gradientStart = if (isDark) Color(0xFF000000) else Color(0xFFF5F5F2),
                gradientEnd = if (isDark) Color(0xFF0A0A0A) else Color(0xFFEBEBE6),
                gradientAngleDeg = 180f,
                useGradientBackground = false,
                cardAlpha = 1f,
                blurIntensity = 0f,
                cornerRadiusDp = 16f,
                density = UiDensity.COMFORTABLE,
                fontChoice = FontChoice.THEME_DEFAULT,
                navStyle = NavStyle.NOTHING_DOT_STRIP,
                playerLayoutStyle = PlayerLayoutStyle.NOTHING_GLYPH_DIAL,
                artworkShapeStyle = ArtworkShapeStyle.CIRCLE_VINYL,
                cardStyle = CardStyle.NOTHING_DOTTED_OUTLINE,
                backgroundTreatment = BackgroundTreatment.DOT_MATRIX_GRID
            )

            SonaraThemeId.IOS_PREMIUM -> SonaraThemeSpec(
                id = id,
                isDark = isDark,
                primary = Color(0xFFFA2D48),
                secondary = Color(0xFFFF6482),
                accent = Color(0xFFFA2D48),
                background = if (isDark) Color(0xFF0C0C0E) else Color(0xFFF9F9FB),
                backgroundSecondary = if (isDark) Color(0xFF18181C) else Color(0xFFEFEFF4),
                surface = if (isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF),
                surfaceElevated = if (isDark) Color(0xFF2C2C2E) else Color(0xFFF2F2F7),
                textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF000000),
                textSecondary = if (isDark) Color(0xFF8E8E93) else Color(0xFF636366),
                borderColor = if (isDark) Color(0x33FFFFFF) else Color(0x22000000),
                gradientStart = if (isDark) Color(0xFF141118) else Color(0xFFFFFFFF),
                gradientEnd = if (isDark) Color(0xFF09090B) else Color(0xFFF2F2F7),
                gradientAngleDeg = 180f,
                useGradientBackground = true,
                cardAlpha = 0.84f,
                blurIntensity = 0.85f,
                cornerRadiusDp = 16f,
                density = UiDensity.COMFORTABLE,
                fontChoice = FontChoice.THEME_DEFAULT,
                navStyle = NavStyle.IOS_TRANSLUCENT_TAB,
                playerLayoutStyle = PlayerLayoutStyle.IOS_BLUR_SHEET,
                artworkShapeStyle = ArtworkShapeStyle.ROUNDED_SOFT,
                cardStyle = CardStyle.IOS_HAIRLINE,
                backgroundTreatment = BackgroundTreatment.SUBTLE_BLUR_WASH
            )

            SonaraThemeId.NEON_CYBERPUNK -> SonaraThemeSpec(
                id = id,
                isDark = true, // Cyberpunk defaults vivid dark even if light requested; honors mode if explicitly toggled
                primary = Color(0xFF00F0FF),
                secondary = Color(0xFFFF00AA),
                accent = Color(0xFFFCEE09),
                background = if (isDark) Color(0xFF060510) else Color(0xFF120F26),
                backgroundSecondary = if (isDark) Color(0xFF0E0B22) else Color(0xFF1A1536),
                surface = Color(0xFF100E24),
                surfaceElevated = Color(0xFF191538),
                textPrimary = Color(0xFFF0FDFF),
                textSecondary = Color(0xFF94A3F8),
                borderColor = Color(0xFF00F0FF),
                gradientStart = Color(0xFF05040E),
                gradientEnd = Color(0xFF1B0930),
                gradientAngleDeg = 150f,
                useGradientBackground = true,
                cardAlpha = 0.86f,
                blurIntensity = 0.6f,
                cornerRadiusDp = 10f,
                density = UiDensity.COMPACT,
                fontChoice = FontChoice.THEME_DEFAULT,
                navStyle = NavStyle.CYBER_CHAMFER_DOCK,
                playerLayoutStyle = PlayerLayoutStyle.CYBERPUNK_HUD_DECK,
                artworkShapeStyle = ArtworkShapeStyle.CHAMFERED_HEX,
                cardStyle = CardStyle.CYBER_CHAMFER_GLOW,
                backgroundTreatment = BackgroundTreatment.CYBER_SCANLINE_GRID
            )

            SonaraThemeId.LIQUID_AURORA -> SonaraThemeSpec(
                id = id,
                isDark = isDark,
                primary = Color(0xFF34D399),
                secondary = Color(0xFF818CF8),
                accent = Color(0xFFF472B6),
                background = if (isDark) Color(0xFF041218) else Color(0xFFE6FFFA),
                backgroundSecondary = if (isDark) Color(0xFF0A2430) else Color(0xFFE0E7FF),
                surface = if (isDark) Color(0xFF0D222D) else Color(0xFFFFFFFF),
                surfaceElevated = if (isDark) Color(0xFF153240) else Color(0xFFF0FDFA),
                textPrimary = if (isDark) Color(0xFFECFDF5) else Color(0xFF06202A),
                textSecondary = if (isDark) Color(0xFF99F6E4) else Color(0xFF155E75),
                borderColor = if (isDark) Color(0x5534D399) else Color(0x550D9488),
                gradientStart = if (isDark) Color(0xFF03191E) else Color(0xFFD1FAE5),
                gradientEnd = if (isDark) Color(0xFF1E103C) else Color(0xFFE0E7FF),
                gradientAngleDeg = 125f,
                useGradientBackground = true,
                cardAlpha = 0.52f,
                blurIntensity = 0.8f,
                cornerRadiusDp = 28f,
                density = UiDensity.COMFORTABLE,
                fontChoice = FontChoice.THEME_DEFAULT,
                navStyle = NavStyle.AURORA_CAPSULE,
                playerLayoutStyle = PlayerLayoutStyle.AURORA_WAVE_STAGE,
                artworkShapeStyle = ArtworkShapeStyle.ROUNDED_SOFT,
                cardStyle = CardStyle.AURORA_GRADIENT_FILL,
                backgroundTreatment = BackgroundTreatment.FLOWING_AURORA_CANVAS
            )

            SonaraThemeId.MINIMAL_LUXURY -> SonaraThemeSpec(
                id = id,
                isDark = isDark,
                primary = Color(0xFFD4AF37),
                secondary = Color(0xFFF3E5AB),
                accent = Color(0xFFC59B27),
                background = if (isDark) Color(0xFF0B0A08) else Color(0xFFFAF7F0),
                backgroundSecondary = if (isDark) Color(0xFF15130F) else Color(0xFFF2ECE0),
                surface = if (isDark) Color(0xFF14120E) else Color(0xFFFFFFFF),
                surfaceElevated = if (isDark) Color(0xFF1E1B15) else Color(0xFFF7F2E8),
                textPrimary = if (isDark) Color(0xFFF8F4EC) else Color(0xFF1A1712),
                textSecondary = if (isDark) Color(0xFFB5A992) else Color(0xFF6B6252),
                borderColor = Color(0x66D4AF37),
                gradientStart = if (isDark) Color(0xFF0B0A08) else Color(0xFFFAF7F0),
                gradientEnd = if (isDark) Color(0xFF18140C) else Color(0xFFEFE6D5),
                gradientAngleDeg = 160f,
                useGradientBackground = true,
                cardAlpha = 0.96f,
                blurIntensity = 0.15f,
                cornerRadiusDp = 12f,
                density = UiDensity.SPACIOUS,
                fontChoice = FontChoice.THEME_DEFAULT,
                navStyle = NavStyle.LUXURY_HAIRLINE_BAR,
                playerLayoutStyle = PlayerLayoutStyle.LUXURY_TURNTABLE,
                artworkShapeStyle = ArtworkShapeStyle.CIRCLE_VINYL,
                cardStyle = CardStyle.LUXURY_GOLD_BORDER,
                backgroundTreatment = BackgroundTreatment.WARM_OBSIDIAN_GRAIN
            )

            SonaraThemeId.AMOLED_BLACK -> SonaraThemeSpec(
                id = id,
                isDark = true,
                primary = Color(0xFF38BDF8),
                secondary = Color(0xFFFFFFFF),
                accent = Color(0xFF10B981),
                background = Color(0xFF000000),
                backgroundSecondary = Color(0xFF000000),
                surface = Color(0xFF070707),
                surfaceElevated = Color(0xFF111111),
                textPrimary = Color(0xFFFFFFFF),
                textSecondary = Color(0xFF8A8A8E),
                borderColor = Color(0xFF1F1F22),
                gradientStart = Color(0xFF000000),
                gradientEnd = Color(0xFF000000),
                gradientAngleDeg = 180f,
                useGradientBackground = false,
                cardAlpha = 1f,
                blurIntensity = 0f,
                cornerRadiusDp = 14f,
                density = UiDensity.COMPACT,
                fontChoice = FontChoice.THEME_DEFAULT,
                navStyle = NavStyle.AMOLED_MINIMAL_BAR,
                playerLayoutStyle = PlayerLayoutStyle.AMOLED_PURE_FOCUS,
                artworkShapeStyle = ArtworkShapeStyle.ROUNDED_SOFT,
                cardStyle = CardStyle.AMOLED_OUTLINE,
                backgroundTreatment = BackgroundTreatment.TRUE_AMOLED_BLACK
            )

            SonaraThemeId.RETRO_CASSETTE -> SonaraThemeSpec(
                id = id,
                isDark = isDark,
                primary = Color(0xFFF59E0B),
                secondary = Color(0xFFEA580C),
                accent = Color(0xFFEF4444),
                background = if (isDark) Color(0xFF1B1714) else Color(0xFFF2E9D8),
                backgroundSecondary = if (isDark) Color(0xFF26201B) else Color(0xFFE6D8C0),
                surface = if (isDark) Color(0xFF28221D) else Color(0xFFFAF4E8),
                surfaceElevated = if (isDark) Color(0xFF342C25) else Color(0xFFEDE1CA),
                textPrimary = if (isDark) Color(0xFFFAF0DC) else Color(0xFF231C16),
                textSecondary = if (isDark) Color(0xFFCBBBA0) else Color(0xFF6E5E4E),
                borderColor = if (isDark) Color(0xFF5A4B3C) else Color(0xFFB5A288),
                gradientStart = if (isDark) Color(0xFF1B1714) else Color(0xFFF2E9D8),
                gradientEnd = if (isDark) Color(0xFF2B2118) else Color(0xFFE3D3B8),
                gradientAngleDeg = 180f,
                useGradientBackground = true,
                cardAlpha = 0.98f,
                blurIntensity = 0f,
                cornerRadiusDp = 10f,
                density = UiDensity.COMFORTABLE,
                fontChoice = FontChoice.THEME_DEFAULT,
                navStyle = NavStyle.CASSETTE_MECHANICAL_DECK,
                playerLayoutStyle = PlayerLayoutStyle.RETRO_CASSETTE_DECK,
                artworkShapeStyle = ArtworkShapeStyle.CASSETTE_SHELL,
                cardStyle = CardStyle.CASSETTE_PLASTIC_BEVEL,
                backgroundTreatment = BackgroundTreatment.VINTAGE_TAPE_TEXTURE
            )

            SonaraThemeId.WINDOWS_FLUENT -> SonaraThemeSpec(
                id = id,
                isDark = isDark,
                primary = Color(0xFF60CDFF),
                secondary = Color(0xFF0078D4),
                accent = Color(0xFF4CC2FF),
                background = if (isDark) Color(0xFF191B20) else Color(0xFFF3F3F3),
                backgroundSecondary = if (isDark) Color(0xFF20232A) else Color(0xFFEAECEF),
                surface = if (isDark) Color(0xFF262932) else Color(0xFFFFFFFF),
                surfaceElevated = if (isDark) Color(0xFF2E323D) else Color(0xFFF9F9F9),
                textPrimary = if (isDark) Color(0xFFFFFFFF) else Color(0xFF1B1B1B),
                textSecondary = if (isDark) Color(0xFFA8B0BE) else Color(0xFF5D5D5D),
                borderColor = if (isDark) Color(0x33FFFFFF) else Color(0x22000000),
                gradientStart = if (isDark) Color(0xFF181A20) else Color(0xFFF3F5F8),
                gradientEnd = if (isDark) Color(0xFF1F2430) else Color(0xFFE8EDF5),
                gradientAngleDeg = 120f,
                useGradientBackground = true,
                cardAlpha = 0.78f,
                blurIntensity = 0.65f,
                cornerRadiusDp = 8f,
                density = UiDensity.COMFORTABLE,
                fontChoice = FontChoice.THEME_DEFAULT,
                navStyle = NavStyle.FLUENT_ACRYLIC_BAR,
                playerLayoutStyle = PlayerLayoutStyle.FLUENT_STUDIO_PANEL,
                artworkShapeStyle = ArtworkShapeStyle.ROUNDED_SOFT,
                cardStyle = CardStyle.FLUENT_ACRYLIC,
                backgroundTreatment = BackgroundTreatment.FLUENT_MICA_LAYERS
            )

            SonaraThemeId.SONY_WALKMAN -> SonaraThemeSpec(
                id = id,
                isDark = true,
                primary = Color(0xFFE08E45),
                secondary = Color(0xFFFBBF24),
                accent = Color(0xFFF59E0B),
                background = Color(0xFF111317),
                backgroundSecondary = Color(0xFF1A1D24),
                surface = Color(0xFF1E222A),
                surfaceElevated = Color(0xFF272C36),
                textPrimary = Color(0xFFF3F4F6),
                textSecondary = Color(0xFF9CA3AF),
                borderColor = Color(0xFF3E4554),
                gradientStart = Color(0xFF101216),
                gradientEnd = Color(0xFF1D2028),
                gradientAngleDeg = 145f,
                useGradientBackground = true,
                cardAlpha = 0.96f,
                blurIntensity = 0.1f,
                cornerRadiusDp = 12f,
                density = UiDensity.COMFORTABLE,
                fontChoice = FontChoice.THEME_DEFAULT,
                navStyle = NavStyle.WALKMAN_COPPER_RAIL,
                playerLayoutStyle = PlayerLayoutStyle.WALKMAN_JOG_WHEEL,
                artworkShapeStyle = ArtworkShapeStyle.SQUIRCLE_LARGE,
                cardStyle = CardStyle.WALKMAN_BRUSHED_METAL,
                backgroundTreatment = BackgroundTreatment.BRUSHED_TITANIUM_DARK
            )

            SonaraThemeId.NEUMORPHISM -> SonaraThemeSpec(
                id = id,
                isDark = isDark,
                primary = if (isDark) Color(0xFF6366F1) else Color(0xFF4F46E5),
                secondary = if (isDark) Color(0xFF818CF8) else Color(0xFF6366F1),
                accent = Color(0xFF06B6D4),
                background = if (isDark) Color(0xFF23272F) else Color(0xFFE4E9F2),
                backgroundSecondary = if (isDark) Color(0xFF1E2229) else Color(0xFFDCE2EC),
                surface = if (isDark) Color(0xFF272C35) else Color(0xFFE8EDF6),
                surfaceElevated = if (isDark) Color(0xFF2D333D) else Color(0xFFEDF1F9),
                textPrimary = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B),
                textSecondary = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                borderColor = if (isDark) Color(0x33FFFFFF) else Color(0x99FFFFFF),
                gradientStart = if (isDark) Color(0xFF23272F) else Color(0xFFE4E9F2),
                gradientEnd = if (isDark) Color(0xFF1D2027) else Color(0xFFD9E0EC),
                gradientAngleDeg = 135f,
                useGradientBackground = false,
                cardAlpha = 1f,
                blurIntensity = 0f,
                cornerRadiusDp = 22f,
                density = UiDensity.SPACIOUS,
                fontChoice = FontChoice.THEME_DEFAULT,
                navStyle = NavStyle.NEUMORPHIC_EMBOSSED_BAR,
                playerLayoutStyle = PlayerLayoutStyle.NEUMORPHIC_TACTILE_PAD,
                artworkShapeStyle = ArtworkShapeStyle.CIRCLE_VINYL,
                cardStyle = CardStyle.NEUMORPHIC_SOFT,
                backgroundTreatment = BackgroundTreatment.MONOCHROME_SOFT_CLAY
            )

            SonaraThemeId.FUTURISTIC_HOLOGRAPHIC -> SonaraThemeSpec(
                id = id,
                isDark = isDark,
                primary = Color(0xFFA855F7),
                secondary = Color(0xFF38BDF8),
                accent = Color(0xFFF472B6),
                background = if (isDark) Color(0xFF090717) else Color(0xFFF3F0FF),
                backgroundSecondary = if (isDark) Color(0xFF130E2E) else Color(0xFFE0F2FE),
                surface = if (isDark) Color(0xFF171336) else Color(0xFFFFFFFF),
                surfaceElevated = if (isDark) Color(0xFF221C4D) else Color(0xFFF5F3FF),
                textPrimary = if (isDark) Color(0xFFFDF4FF) else Color(0xFF1E1B4B),
                textSecondary = if (isDark) Color(0xFFC4B5FD) else Color(0xFF4C1D95),
                borderColor = Color(0x8838BDF8),
                gradientStart = if (isDark) Color(0xFF0B071C) else Color(0xFFEDE9FE),
                gradientEnd = if (isDark) Color(0xFF071B2C) else Color(0xFFE0F2FE),
                gradientAngleDeg = 120f,
                useGradientBackground = true,
                cardAlpha = 0.68f,
                blurIntensity = 0.72f,
                cornerRadiusDp = 22f,
                density = UiDensity.COMFORTABLE,
                fontChoice = FontChoice.THEME_DEFAULT,
                navStyle = NavStyle.HOLO_PRISM_DOCK,
                playerLayoutStyle = PlayerLayoutStyle.HOLOGRAPHIC_3D_PRISM,
                artworkShapeStyle = ArtworkShapeStyle.TILTED_HOLO_CARD,
                cardStyle = CardStyle.HOLOGRAPHIC_IRIDESCENT,
                backgroundTreatment = BackgroundTreatment.PRISM_IRIDESCENT_SWEEP
            )

            SonaraThemeId.EDITORIAL_MUSIC -> SonaraThemeSpec(
                id = id,
                isDark = isDark,
                primary = if (isDark) Color(0xFFF43F5E) else Color(0xFFBE123C),
                secondary = if (isDark) Color(0xFFE5E5E5) else Color(0xFF171717),
                accent = Color(0xFFF43F5E),
                background = if (isDark) Color(0xFF100F0E) else Color(0xFFFAF8F5),
                backgroundSecondary = if (isDark) Color(0xFF181614) else Color(0xFFF1ECE4),
                surface = if (isDark) Color(0xFF181615) else Color(0xFFFFFFFF),
                surfaceElevated = if (isDark) Color(0xFF221F1D) else Color(0xFFF5F0E8),
                textPrimary = if (isDark) Color(0xFFFAF7F2) else Color(0xFF111111),
                textSecondary = if (isDark) Color(0xFFA8A29E) else Color(0xFF57534E),
                borderColor = if (isDark) Color(0xFF44403C) else Color(0xFFD6D3D1),
                gradientStart = if (isDark) Color(0xFF100F0E) else Color(0xFFFAF8F5),
                gradientEnd = if (isDark) Color(0xFF171412) else Color(0xFFF3EEE6),
                gradientAngleDeg = 180f,
                useGradientBackground = false,
                cardAlpha = 1f,
                blurIntensity = 0f,
                cornerRadiusDp = 4f,
                density = UiDensity.SPACIOUS,
                fontChoice = FontChoice.THEME_DEFAULT,
                navStyle = NavStyle.EDITORIAL_INDEX_STRIP,
                playerLayoutStyle = PlayerLayoutStyle.EDITORIAL_COVER_SPREAD,
                artworkShapeStyle = ArtworkShapeStyle.SHARP_EDITORIAL,
                cardStyle = CardStyle.EDITORIAL_RULED,
                backgroundTreatment = BackgroundTreatment.EDITORIAL_PAPER_CONTRAST
            )
        }
    }

    fun resolveThemeSpec(
        customizations: ThemeCustomizations,
        isSystemDark: Boolean,
        dynamicArtColors: DynamicArtColors?
    ): SonaraThemeSpec {
        val themeId = runCatching { SonaraThemeId.valueOf(customizations.themeId) }
            .getOrDefault(SonaraThemeId.GLASSMORPHISM_PRO)

        val appearance = runCatching { AppearanceMode.valueOf(customizations.appearanceMode) }
            .getOrDefault(AppearanceMode.DARK)

        val isDark = when (appearance) {
            AppearanceMode.DARK -> true
            AppearanceMode.LIGHT -> false
            AppearanceMode.AUTO -> isSystemDark
        }

        val base = defaultSpecFor(themeId, isDark)
        val density = runCatching { UiDensity.valueOf(customizations.uiDensity) }
            .getOrDefault(base.density)
        val fontChoice = runCatching { FontChoice.valueOf(customizations.fontChoice) }
            .getOrDefault(base.fontChoice)
        val preferredArabicFont = runCatching { FontChoice.valueOf(customizations.preferredArabicFont) }
            .getOrDefault(FontChoice.CAIRO)

        var resolved = base.copy(
            cardAlpha = customizations.cardTransparency.coerceIn(0.22f, 1f),
            blurIntensity = customizations.blurIntensity.coerceIn(0f, 1f),
            cornerRadiusDp = customizations.cornerRadiusDp.coerceIn(0f, 32f),
            density = density,
            fontChoice = fontChoice,
            preferredArabicFont = preferredArabicFont
        )

        if (customizations.useCustomColors) {
            resolved = resolved.copy(
                primary = Color(customizations.customPrimaryHex),
                secondary = Color(customizations.customSecondaryHex),
                accent = Color(customizations.customAccentHex),
                background = Color(customizations.customBackgroundHex),
                surface = Color(customizations.customSurfaceHex),
                surfaceElevated = lerp(Color(customizations.customSurfaceHex), Color.White, if (isDark) 0.08f else 0.04f),
                textPrimary = Color(customizations.customTextHex)
            )
        }

        if (customizations.useCustomGradient) {
            resolved = resolved.copy(
                useGradientBackground = true,
                gradientStart = Color(customizations.gradientStartHex),
                gradientEnd = Color(customizations.gradientEndHex),
                gradientAngleDeg = customizations.gradientAngleDeg
            )
        }

        // Dynamic Album Art Palette blending (if enabled & available, gently harmonizes accent/gradient)
        if (customizations.dynamicAlbumArtColors && dynamicArtColors != null && !customizations.useCustomColors) {
            resolved = resolved.copy(
                primary = lerp(resolved.primary, dynamicArtColors.vibrant, 0.55f),
                accent = lerp(resolved.accent, dynamicArtColors.lightVibrant, 0.50f),
                gradientEnd = if (isDark) {
                    lerp(resolved.gradientEnd, dynamicArtColors.darkVibrant, 0.65f)
                } else {
                    lerp(resolved.gradientEnd, dynamicArtColors.lightVibrant, 0.28f)
                }
            )
        }

        return resolved
    }

    fun curatedPalettesFor(themeId: SonaraThemeId): List<ThemePalettePreset> {
        return when (themeId) {
            SonaraThemeId.GLASSMORPHISM_PRO -> listOf(
                ThemePalettePreset("gl_arctic", "Arctic Prism", "موشور قطبي", 0xFF38BDF8, 0xFF8B5CF6, 0xFF22D3EE, 0xFF070B19, 0xFF16203C, 0xFFF8FAFC),
                ThemePalettePreset("gl_rose", "Rose Quartz", "كوارتز وردي", 0xFFF472B6, 0xFFA855F7, 0xFFFB7185, 0xFF150819, 0xFF281230, 0xFFFDF2F8),
                ThemePalettePreset("gl_emerald", "Emerald Mist", "ضباب زمردي", 0xFF34D399, 0xFF06B6D4, 0xFF6EE7B7, 0xFF041414, 0xFF0D2829, 0xFFECFDF5)
            )
            SonaraThemeId.NEON_CYBERPUNK -> listOf(
                ThemePalettePreset("cy_tokyo", "Night City", "مدينة الليل", 0xFF00F0FF, 0xFFFF00AA, 0xFFFCEE09, 0xFF060510, 0xFF100E24, 0xFFF0FDFF),
                ThemePalettePreset("cy_crimson", "Akira Red", "أحمر أكيرا", 0xFFFF2A54, 0xFF00E5FF, 0xFFFF9E00, 0xFF0C0408, 0xFF1A0912, 0xFFFFF1F2),
                ThemePalettePreset("cy_acid", "Acid Matrix", "ماتريكس نيون", 0xFFA3E635, 0xFF22D3EE, 0xFFFACC15, 0xFF040B06, 0xFF0C1B10, 0xFFF7FEE7)
            )
            SonaraThemeId.MINIMAL_LUXURY -> listOf(
                ThemePalettePreset("lx_champagne", "Champagne Gold", "ذهب دافئ", 0xFFD4AF37, 0xFFF3E5AB, 0xFFC59B27, 0xFF0B0A08, 0xFF14120E, 0xFFF8F4EC),
                ThemePalettePreset("lx_bronze", "Brushed Bronze", "برونز مصقول", 0xFFCD7F32, 0xFFE6BE8A, 0xFFD97706, 0xFF0E0A07, 0xFF1A130D, 0xFFFAF5EE),
                ThemePalettePreset("lx_platinum", "Platinum Silk", "حرير بلاتيني", 0xFFE2E8F0, 0xFF94A3B8, 0xFFCBD5E1, 0xFF0A0B0E, 0xFF14161C, 0xFFFFFFFF)
            )
            SonaraThemeId.RETRO_CASSETTE -> listOf(
                ThemePalettePreset("rc_amber", "Maxell Amber", "كهرمان كلاسيكي", 0xFFF59E0B, 0xFFEA580C, 0xFFEF4444, 0xFF1B1714, 0xFF28221D, 0xFFFAF0DC),
                ThemePalettePreset("rc_mint", "Walkman Teal", "تركواز ووكمان", 0xFF14B8A6, 0xFFF59E0B, 0xFFFB7185, 0xFF121A1B, 0xFF1B2729, 0xFFECFDF5),
                ThemePalettePreset("rc_cream", "Studio Tape", "شريط استوديو", 0xFFD97706, 0xFFB45309, 0xFFDC2626, 0xFFF2E9D8, 0xFFFAF4E8, 0xFF231C16)
            )
            else -> listOf(
                ThemePalettePreset("p_ocean", "Oceanic Cyan", "سماوي محيطي", 0xFF0EA5E9, 0xFF6366F1, 0xFF38BDF8, 0xFF08101E, 0xFF121E33, 0xFFF8FAFC),
                ThemePalettePreset("p_sunset", "Velvet Sunset", "غروب مخملي", 0xFFF97316, 0xFFEC4899, 0xFFFACC15, 0xFF170B12, 0xFF26121D, 0xFFFFF7ED),
                ThemePalettePreset("p_royal", "Royal Violet", "بنفسجي ملكي", 0xFF8B5CF6, 0xFFEC4899, 0xFFC084FC, 0xFF0C091A, 0xFF181230, 0xFFF5F3FF)
            )
        }
    }
}
