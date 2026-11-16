package com.example.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

enum class SonaraThemeId(
    val displayNameEn: String,
    val displayNameAr: String,
    val subtitleEn: String,
    val subtitleAr: String
) {
    GLASSMORPHISM_PRO(
        "Glassmorphism Pro",
        "الزجاجي المتقدم",
        "Luminous frosted layers & floating glass docks",
        "طبقات زجاجية شفافة وإضاءة ناعمة"
    ),
    SAMSUNG_ONE_UI(
        "Samsung One UI",
        "واجهة ون يو آي",
        "Expansive headers & comfortable squircle panels",
        "عناوين مريحة ولوحات دائرية متناسقة"
    ),
    GOOGLE_PIXEL_MATERIAL_YOU(
        "Pixel Material You",
        "بيكسل ماتيريال يو",
        "Expressive organic shapes & dynamic color surfaces",
        "أشكال عضوية وألوان تفاعلية ديناميكية"
    ),
    NOTHING_OS(
        "Nothing OS",
        "نوثينج أو إس",
        "Monochrome matrix typography & signature red dots",
        "أحادي اللون مع نقاط حمراء وتقنية نقطية"
    ),
    IOS_PREMIUM(
        "iOS Premium",
        "آي أو إس بريميوم",
        "Translucent chrome, hairline dividers & crisp depth",
        "شفافية أنيقة وفواصل دقيقة وعمق بصري"
    ),
    NEON_CYBERPUNK(
        "Neon Cyberpunk",
        "نيون سايبربانك",
        "Electric cyan-magenta glow & chamfered HUD cards",
        "إضاءة نيون كهربائية وزوايا هندسية حادة"
    ),
    LIQUID_AURORA(
        "Liquid Aurora",
        "الشفق السائل",
        "Flowing multi-stop aurora gradients & fluid motion",
        "تدرجات شفقية متدفقة وحركة انسيابية"
    ),
    MINIMAL_LUXURY(
        "Minimal Luxury",
        "الفخامة البسيطة",
        "Obsidian & champagne gold with editorial serif",
        "ذهب دافئ وخطوط فاخرة ومساحات واسعة"
    ),
    AMOLED_BLACK(
        "AMOLED Black",
        "أموليد الأسود",
        "True pixel-off #000000 canvas & razor-sharp contrast",
        "أسود نقي لتوفير البطارية وتباين فائق"
    ),
    RETRO_CASSETTE(
        "Retro Cassette",
        "كاسيت كلاسيكي",
        "Analog deck dials, VU warmth & spinning cassette tape",
        "شريط كاسيت دوّار وتفاصيل تناظرية دافئة"
    ),
    WINDOWS_FLUENT(
        "Windows Fluent",
        "ويندوز فلوينت",
        "Mica acrylic geometry, crisp highlights & left accents",
        "طبقات أكريليك هندسية وحواف مضيئة"
    ),
    SONY_WALKMAN(
        "Sony Walkman",
        "سوني ووكمان",
        "Brushed titanium deck, copper accents & rotary dial",
        "تيتانيوم مصقول وقرص تحكم دائري نحاسي"
    ),
    NEUMORPHISM(
        "Neumorphism",
        "نيومورفيزم",
        "Soft embossed tactile surfaces & dual light-shadow depth",
        "أسطح ناعمة بارزة وظلال مجسمة ملموسة"
    ),
    FUTURISTIC_HOLOGRAPHIC(
        "Holographic",
        "هولوغرافي مستقبلي",
        "Iridescent prism foil sheen & dimensional cards",
        "انعكاسات قزحية ثلاثية الأبعاد وإطارات مضيئة"
    ),
    EDITORIAL_MUSIC(
        "Editorial Music",
        "المجلة الموسيقية",
        "Magazine serif mastheads, oversized art & stark rules",
        "تخطيط مجلة أنيق وعناوين عريضة وغلاف ضخم"
    )
}

enum class AppearanceMode(val labelEn: String, val labelAr: String) {
    DARK("Dark", "داكن"),
    LIGHT("Light", "فاتح"),
    AUTO("System", "تلقائي")
}

enum class UiDensity(val labelEn: String, val labelAr: String, val scale: Float) {
    COMPACT("Compact", "مضغوط", 0.86f),
    COMFORTABLE("Comfortable", "مريح", 1.0f),
    SPACIOUS("Spacious", "واسع", 1.16f)
}

enum class FontChoice(val labelEn: String, val labelAr: String, val isArabicFriendly: Boolean = false) {
    THEME_DEFAULT("Theme Default", "افتراضي الثيم", true),
    PLUS_JAKARTA("Plus Jakarta Sans", "جاكرتا سانس"),
    SPACE_GROTESK("Space Grotesk", "سبيس غروتيسك"),
    PLAYFAIR("Playfair Display", "بلاي فير سيريف"),
    JETBRAINS_MONO("JetBrains Mono", "جيت برينز مونو"),
    CAIRO("Cairo", "القاهرة (Cairo)", true),
    TAJAWAL("Tajawal", "تجوال (Tajawal)", true),
    IBM_PLEX_ARABIC("IBM Plex Sans Arabic", "آي بي إم بليكس عربي", true),
    NOTO_SANS_ARABIC("Noto Sans Arabic", "نوتو سانس عربي", true),
    AMIRI("Amiri Editorial", "أميري كلاسيكي (Amiri)", true)
}

enum class NavStyle {
    FLOATING_GLASS_PILL,
    ONE_UI_ROUNDED_DOCK,
    MATERIAL_YOU_BAR,
    NOTHING_DOT_STRIP,
    IOS_TRANSLUCENT_TAB,
    CYBER_CHAMFER_DOCK,
    AURORA_CAPSULE,
    LUXURY_HAIRLINE_BAR,
    AMOLED_MINIMAL_BAR,
    CASSETTE_MECHANICAL_DECK,
    FLUENT_ACRYLIC_BAR,
    WALKMAN_COPPER_RAIL,
    NEUMORPHIC_EMBOSSED_BAR,
    HOLO_PRISM_DOCK,
    EDITORIAL_INDEX_STRIP
}

enum class PlayerLayoutStyle {
    GLASS_FLOATING_DOCK,
    ONE_UI_BOTTOM_ANCHORED,
    PIXEL_EXPRESSIVE_CARD,
    NOTHING_GLYPH_DIAL,
    IOS_BLUR_SHEET,
    CYBERPUNK_HUD_DECK,
    AURORA_WAVE_STAGE,
    LUXURY_TURNTABLE,
    AMOLED_PURE_FOCUS,
    RETRO_CASSETTE_DECK,
    FLUENT_STUDIO_PANEL,
    WALKMAN_JOG_WHEEL,
    NEUMORPHIC_TACTILE_PAD,
    HOLOGRAPHIC_3D_PRISM,
    EDITORIAL_COVER_SPREAD
}

enum class ArtworkShapeStyle {
    ROUNDED_SOFT,
    SQUIRCLE_LARGE,
    CIRCLE_VINYL,
    CASSETTE_SHELL,
    CHAMFERED_HEX,
    SHARP_EDITORIAL,
    SCALLOPED_FLOWER,
    TILTED_HOLO_CARD
}

enum class CardStyle {
    TRANSLUCENT_GLASS,
    ONE_UI_PANEL,
    MATERIAL_TONAL,
    NOTHING_DOTTED_OUTLINE,
    IOS_HAIRLINE,
    CYBER_CHAMFER_GLOW,
    AURORA_GRADIENT_FILL,
    LUXURY_GOLD_BORDER,
    AMOLED_OUTLINE,
    CASSETTE_PLASTIC_BEVEL,
    FLUENT_ACRYLIC,
    WALKMAN_BRUSHED_METAL,
    NEUMORPHIC_SOFT,
    HOLOGRAPHIC_IRIDESCENT,
    EDITORIAL_RULED
}

enum class BackgroundTreatment {
    RADIAL_GLOW_MESH,
    CLEAN_TONAL,
    DYNAMIC_PILL_CANVAS,
    DOT_MATRIX_GRID,
    SUBTLE_BLUR_WASH,
    CYBER_SCANLINE_GRID,
    FLOWING_AURORA_CANVAS,
    WARM_OBSIDIAN_GRAIN,
    TRUE_AMOLED_BLACK,
    VINTAGE_TAPE_TEXTURE,
    FLUENT_MICA_LAYERS,
    BRUSHED_TITANIUM_DARK,
    MONOCHROME_SOFT_CLAY,
    PRISM_IRIDESCENT_SWEEP,
    EDITORIAL_PAPER_CONTRAST
}

data class ThemePalettePreset(
    val id: String,
    val name: String,
    val nameAr: String,
    val primaryHex: Long,
    val secondaryHex: Long,
    val accentHex: Long,
    val backgroundHex: Long,
    val surfaceHex: Long,
    val textHex: Long
)

@Serializable
data class ThemeCustomizations(
    val themeId: String = SonaraThemeId.GLASSMORPHISM_PRO.name,
    val appearanceMode: String = AppearanceMode.DARK.name,
    val useCustomColors: Boolean = false,
    val customPrimaryHex: Long = 0xFF38BDF8,
    val customSecondaryHex: Long = 0xFF8B5CF6,
    val customAccentHex: Long = 0xFF22D3EE,
    val customBackgroundHex: Long = 0xFF090D1A,
    val customSurfaceHex: Long = 0xFF131B2E,
    val customTextHex: Long = 0xFFF8FAFC,
    val useCustomGradient: Boolean = false,
    val gradientStartHex: Long = 0xFF0F172A,
    val gradientEndHex: Long = 0xFF1E1B4B,
    val gradientAngleDeg: Float = 135f,
    val cardTransparency: Float = 0.72f,
    val blurIntensity: Float = 0.65f,
    val cornerRadiusDp: Float = 20f,
    val uiDensity: String = UiDensity.COMFORTABLE.name,
    val fontChoice: String = FontChoice.THEME_DEFAULT.name,
    val preferredArabicFont: String = FontChoice.CAIRO.name,
    val dynamicAlbumArtColors: Boolean = true
)

@Serializable
data class WidgetCustomizations(
    val widgetThemeId: String = SonaraThemeId.GLASSMORPHISM_PRO.name,
    val backgroundColorHex: Long = 0xFF111625,
    val textColorHex: Long = 0xFFFFFFFF,
    val accentColorHex: Long = 0xFF38BDF8,
    val transparency: Float = 0.85f,
    val cornerRadiusDp: Float = 22f,
    val showArtwork: Boolean = true,
    val showProgress: Boolean = true
)

data class SonaraThemeSpec(
    val id: SonaraThemeId,
    val isDark: Boolean,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val background: Color,
    val backgroundSecondary: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val borderColor: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val gradientAngleDeg: Float,
    val useGradientBackground: Boolean,
    val cardAlpha: Float,
    val blurIntensity: Float,
    val cornerRadiusDp: Float,
    val density: UiDensity,
    val fontChoice: FontChoice,
    val preferredArabicFont: FontChoice = FontChoice.CAIRO,
    val navStyle: NavStyle,
    val playerLayoutStyle: PlayerLayoutStyle,
    val artworkShapeStyle: ArtworkShapeStyle,
    val cardStyle: CardStyle,
    val backgroundTreatment: BackgroundTreatment
)
