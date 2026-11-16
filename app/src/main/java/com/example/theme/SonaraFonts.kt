package com.example.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.FontChoice
import com.example.model.SonaraThemeId

object SonaraFonts {
    val PlusJakartaSans = FontFamily(Font(R.font.plus_jakarta_sans, FontWeight.Normal))
    val Cairo = FontFamily(Font(R.font.cairo, FontWeight.Normal))
    val SpaceGrotesk = FontFamily(Font(R.font.space_grotesk, FontWeight.Normal))
    val PlayfairDisplay = FontFamily(Font(R.font.playfair_display, FontWeight.Normal))
    val JetBrainsMono = FontFamily(Font(R.font.jetbrains_mono, FontWeight.Normal))
    val Amiri = FontFamily(Font(R.font.amiri, FontWeight.Normal))

    fun resolveFamilies(
        themeId: SonaraThemeId,
        fontChoice: FontChoice,
        isArabic: Boolean
    ): Pair<FontFamily, FontFamily> {
        // Explicit user font choice overrides theme default
        when (fontChoice) {
            FontChoice.PLUS_JAKARTA -> return PlusJakartaSans to PlusJakartaSans
            FontChoice.SPACE_GROTESK -> return SpaceGrotesk to PlusJakartaSans
            FontChoice.PLAYFAIR -> return (if (isArabic) Amiri else PlayfairDisplay) to (if (isArabic) Cairo else PlusJakartaSans)
            FontChoice.JETBRAINS_MONO -> return JetBrainsMono to JetBrainsMono
            FontChoice.CAIRO -> return Cairo to Cairo
            FontChoice.AMIRI -> return Amiri to Cairo
            FontChoice.THEME_DEFAULT -> Unit
        }

        if (isArabic) {
            return when (themeId) {
                SonaraThemeId.MINIMAL_LUXURY,
                SonaraThemeId.EDITORIAL_MUSIC -> Amiri to Cairo
                SonaraThemeId.NOTHING_OS,
                SonaraThemeId.RETRO_CASSETTE,
                SonaraThemeId.NEON_CYBERPUNK -> Cairo to JetBrainsMono
                else -> Cairo to Cairo
            }
        }

        return when (themeId) {
            SonaraThemeId.NOTHING_OS,
            SonaraThemeId.RETRO_CASSETTE,
            SonaraThemeId.SONY_WALKMAN -> JetBrainsMono to SpaceGrotesk
            SonaraThemeId.NEON_CYBERPUNK,
            SonaraThemeId.FUTURISTIC_HOLOGRAPHIC -> SpaceGrotesk to PlusJakartaSans
            SonaraThemeId.MINIMAL_LUXURY,
            SonaraThemeId.EDITORIAL_MUSIC -> PlayfairDisplay to PlusJakartaSans
            else -> PlusJakartaSans to PlusJakartaSans
        }
    }

    fun buildTypography(
        display: FontFamily,
        body: FontFamily,
        densityScale: Float = 1.0f
    ): Typography {
        val s = densityScale.coerceIn(0.88f, 1.12f)
        return Typography(
            displayLarge = TextStyle(
                fontFamily = display,
                fontWeight = FontWeight.Bold,
                fontSize = (36f * s).sp,
                lineHeight = (42f * s).sp,
                letterSpacing = (-0.5).sp
            ),
            headlineLarge = TextStyle(
                fontFamily = display,
                fontWeight = FontWeight.Bold,
                fontSize = (28f * s).sp,
                lineHeight = (34f * s).sp
            ),
            headlineMedium = TextStyle(
                fontFamily = display,
                fontWeight = FontWeight.SemiBold,
                fontSize = (22f * s).sp,
                lineHeight = (28f * s).sp
            ),
            titleLarge = TextStyle(
                fontFamily = display,
                fontWeight = FontWeight.Bold,
                fontSize = (19f * s).sp,
                lineHeight = (25f * s).sp
            ),
            titleMedium = TextStyle(
                fontFamily = body,
                fontWeight = FontWeight.SemiBold,
                fontSize = (16f * s).sp,
                lineHeight = (22f * s).sp
            ),
            bodyLarge = TextStyle(
                fontFamily = body,
                fontWeight = FontWeight.Normal,
                fontSize = (15f * s).sp,
                lineHeight = (21f * s).sp
            ),
            bodyMedium = TextStyle(
                fontFamily = body,
                fontWeight = FontWeight.Normal,
                fontSize = (13.5f * s).sp,
                lineHeight = (19f * s).sp
            ),
            labelLarge = TextStyle(
                fontFamily = body,
                fontWeight = FontWeight.SemiBold,
                fontSize = (13f * s).sp,
                lineHeight = (18f * s).sp
            ),
            labelSmall = TextStyle(
                fontFamily = body,
                fontWeight = FontWeight.Medium,
                fontSize = (11f * s).sp,
                lineHeight = (14f * s).sp,
                letterSpacing = 0.6.sp
            )
        )
    }
}
