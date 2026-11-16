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
    val Tajawal = FontFamily(Font(R.font.tajawal, FontWeight.Normal))
    val IbmPlexSansArabic = FontFamily(Font(R.font.ibm_plex_sans_arabic, FontWeight.Normal))
    val NotoSansArabic = FontFamily(Font(R.font.noto_sans_arabic, FontWeight.Normal))
    val SpaceGrotesk = FontFamily(Font(R.font.space_grotesk, FontWeight.Normal))
    val PlayfairDisplay = FontFamily(Font(R.font.playfair_display, FontWeight.Normal))
    val JetBrainsMono = FontFamily(Font(R.font.jetbrains_mono, FontWeight.Normal))
    val Amiri = FontFamily(Font(R.font.amiri, FontWeight.Normal))

    private fun arabicFamilyFor(choice: FontChoice): FontFamily {
        return when (choice) {
            FontChoice.TAJAWAL -> Tajawal
            FontChoice.IBM_PLEX_ARABIC -> IbmPlexSansArabic
            FontChoice.NOTO_SANS_ARABIC -> NotoSansArabic
            FontChoice.AMIRI -> Amiri
            else -> Cairo
        }
    }

    fun resolveFamilies(
        themeId: SonaraThemeId,
        fontChoice: FontChoice,
        preferredArabicFont: FontChoice,
        isArabic: Boolean
    ): Pair<FontFamily, FontFamily> {
        val preferredAr = arabicFamilyFor(preferredArabicFont)

        when (fontChoice) {
            FontChoice.PLUS_JAKARTA -> return (if (isArabic) preferredAr else PlusJakartaSans) to (if (isArabic) preferredAr else PlusJakartaSans)
            FontChoice.SPACE_GROTESK -> return (if (isArabic) preferredAr else SpaceGrotesk) to (if (isArabic) preferredAr else PlusJakartaSans)
            FontChoice.PLAYFAIR -> return (if (isArabic) Amiri else PlayfairDisplay) to (if (isArabic) preferredAr else PlusJakartaSans)
            FontChoice.JETBRAINS_MONO -> return (if (isArabic) IbmPlexSansArabic else JetBrainsMono) to (if (isArabic) preferredAr else JetBrainsMono)
            FontChoice.CAIRO -> return Cairo to Cairo
            FontChoice.TAJAWAL -> return Tajawal to Tajawal
            FontChoice.IBM_PLEX_ARABIC -> return IbmPlexSansArabic to IbmPlexSansArabic
            FontChoice.NOTO_SANS_ARABIC -> return NotoSansArabic to NotoSansArabic
            FontChoice.AMIRI -> return Amiri to preferredAr
            FontChoice.THEME_DEFAULT -> Unit
        }

        if (isArabic) {
            return when (themeId) {
                SonaraThemeId.MINIMAL_LUXURY,
                SonaraThemeId.EDITORIAL_MUSIC -> Amiri to preferredAr
                SonaraThemeId.NOTHING_OS,
                SonaraThemeId.RETRO_CASSETTE,
                SonaraThemeId.NEON_CYBERPUNK,
                SonaraThemeId.SONY_WALKMAN -> IbmPlexSansArabic to preferredAr
                else -> preferredAr to preferredAr
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
                fontSize = (34f * s).sp,
                lineHeight = (44f * s).sp
            ),
            headlineLarge = TextStyle(
                fontFamily = display,
                fontWeight = FontWeight.Bold,
                fontSize = (27f * s).sp,
                lineHeight = (36f * s).sp
            ),
            headlineMedium = TextStyle(
                fontFamily = display,
                fontWeight = FontWeight.SemiBold,
                fontSize = (22f * s).sp,
                lineHeight = (30f * s).sp
            ),
            titleLarge = TextStyle(
                fontFamily = display,
                fontWeight = FontWeight.Bold,
                fontSize = (19f * s).sp,
                lineHeight = (27f * s).sp
            ),
            titleMedium = TextStyle(
                fontFamily = body,
                fontWeight = FontWeight.SemiBold,
                fontSize = (16f * s).sp,
                lineHeight = (24f * s).sp
            ),
            bodyLarge = TextStyle(
                fontFamily = body,
                fontWeight = FontWeight.Normal,
                fontSize = (15f * s).sp,
                lineHeight = (23f * s).sp
            ),
            bodyMedium = TextStyle(
                fontFamily = body,
                fontWeight = FontWeight.Normal,
                fontSize = (13.5f * s).sp,
                lineHeight = (20f * s).sp
            ),
            labelLarge = TextStyle(
                fontFamily = body,
                fontWeight = FontWeight.SemiBold,
                fontSize = (13f * s).sp,
                lineHeight = (19f * s).sp
            ),
            labelSmall = TextStyle(
                fontFamily = body,
                fontWeight = FontWeight.Medium,
                fontSize = (11f * s).sp,
                lineHeight = (15f * s).sp
            )
        )
    }
}
