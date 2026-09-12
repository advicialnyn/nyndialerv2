package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class DialerThemePreset(
    val id: String,
    val title: String,
    val isDark: Boolean,
    val previewPrimary: Color,
    val previewBg: Color
) {
    // Light Themes
    CLEAN_LIGHT("clean_light", "Clean Minimal Light", false, Color(0xFF2563EB), CanvasLight),
    PIXEL_LIGHT("pixel_light", "Pixel Blue Light", false, GoogleBlue, Color(0xFFF8FAFD)),
    MINT_LIGHT("mint_light", "Emerald Mint Light", false, DialEmeraldDark, Color(0xFFF0FDF4)),
    WARM_PAPER("warm_paper", "Warm Paper Ivory", false, Color(0xFFD97706), Color(0xFFFFFBEB)),
    CORAL_LIGHT("coral_light", "Sunset Coral Light", false, SunsetPrimary, Color(0xFFFFF1F2)),

    // Dark Themes
    PITCH_BLACK("pitch_black", "Dark Black (AMOLED)", true, DialEmerald, PitchBlack),
    OBSIDIAN_NAVY("obsidian", "Obsidian Navy", true, DialEmerald, ObsidianDark),
    PIXEL_BLUE_DARK("pixel_dark", "Pixel Blue Dark", true, GoogleBlueLight, GoogleDarkBg),
    CYBER_CYAN("cyber_cyan", "Cyber Cyan", true, SipCyan, CyberCyanBg),
    EMERALD_FOREST("emerald", "Emerald Forest", true, ForestPrimary, ForestBg),
    SUNSET_CORAL("sunset", "Sunset Coral", true, SunsetPrimary, SunsetBg),
    ROYAL_AMETHYST("amethyst", "Royal Amethyst", true, AmethystPrimary, AmethystBg),
    AMBER_GOLD("amber", "Amber Gold", true, AmberPrimary, AmberBg),
    SLATE_MONO("slate", "Monochrome Slate", true, SlatePrimary, SlateBg)
}

// 1. Dark Black (AMOLED Pitch Black)
private val PitchBlackColorScheme = darkColorScheme(
    primary = DialEmerald,
    onPrimary = Color.Black,
    primaryContainer = DialEmeraldDark,
    onPrimaryContainer = Color.White,
    secondary = SipCyan,
    onSecondary = Color.Black,
    secondaryContainer = PitchBlackSurfaceVariant,
    onSecondaryContainer = SipCyan,
    tertiary = WarningAmber,
    background = PitchBlack,
    onBackground = Color.White,
    surface = PitchBlackSurface,
    onSurface = Color.White,
    surfaceVariant = PitchBlackSurfaceVariant,
    onSurfaceVariant = Color(0xFFA3A3A3),
    outline = PitchBlackBorder,
    error = HangupRed,
    onError = Color.White
)

// 2. Obsidian Navy
private val ObsidianColorScheme = darkColorScheme(
    primary = DialEmerald,
    onPrimary = Color.Black,
    primaryContainer = DialEmeraldDark,
    onPrimaryContainer = Color.White,
    secondary = SipCyan,
    onSecondary = Color.Black,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = SipCyan,
    tertiary = WarningAmber,
    background = ObsidianDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark,
    error = HangupRed,
    onError = Color.White
)

// 3. Pixel Blue Dark
private val PixelDarkColorScheme = darkColorScheme(
    primary = GoogleBlueLight,
    onPrimary = Color(0xFF003063),
    primaryContainer = Color(0xFF00468A),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFFA8C7FA),
    onSecondary = Color(0xFF0D3258),
    secondaryContainer = Color(0xFF274870),
    onSecondaryContainer = Color(0xFFD6E3FF),
    tertiary = WarningAmber,
    background = GoogleDarkBg,
    onBackground = Color(0xFFE2E2E6),
    surface = GoogleDarkSurface,
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF2B313A),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF3C4450),
    error = HangupRed,
    onError = Color.White
)

// 4. Pixel Blue Light
private val PixelLightColorScheme = lightColorScheme(
    primary = GoogleBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001B3E),
    secondary = Color(0xFF3F608F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD6E3FF),
    onSecondaryContainer = Color(0xFF001B3F),
    tertiary = WarningAmber,
    background = Color(0xFFF8FAFD),
    onBackground = Color(0xFF191C1E),
    surface = Color.White,
    onSurface = Color(0xFF191C1E),
    surfaceVariant = Color(0xFFEFF2F8),
    onSurfaceVariant = Color(0xFF44474E),
    outline = Color(0xFFC4C6D0),
    error = HangupRed,
    onError = Color.White
)

// 5. Cyber Cyan
private val CyberCyanColorScheme = darkColorScheme(
    primary = SipCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF00566B),
    onPrimaryContainer = Color(0xFFB8EAFF),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color.Black,
    secondaryContainer = CyberCyanSurface,
    onSecondaryContainer = SipCyan,
    background = CyberCyanBg,
    onBackground = Color(0xFFF0F9FF),
    surface = CyberCyanSurface,
    onSurface = Color(0xFFF0F9FF),
    surfaceVariant = Color(0xFF162938),
    onSurfaceVariant = Color(0xFF7DD3FC),
    outline = Color(0xFF1E3A4D),
    error = HangupRed,
    onError = Color.White
)

// 6. Emerald Forest
private val ForestColorScheme = darkColorScheme(
    primary = ForestPrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF065F46),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = Color(0xFF34D399),
    onSecondary = Color.Black,
    secondaryContainer = ForestSurface,
    onSecondaryContainer = ForestPrimary,
    background = ForestBg,
    onBackground = Color(0xFFECFDF5),
    surface = ForestSurface,
    onSurface = Color(0xFFECFDF5),
    surfaceVariant = Color(0xFF143B2A),
    onSurfaceVariant = Color(0xFF6EE7B7),
    outline = Color(0xFF1E4D38),
    error = HangupRed,
    onError = Color.White
)

// 7. Sunset Coral
private val SunsetColorScheme = darkColorScheme(
    primary = SunsetPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF881337),
    onPrimaryContainer = Color(0xFFFFCCD5),
    secondary = Color(0xFFFB7185),
    onSecondary = Color.Black,
    secondaryContainer = SunsetSurface,
    onSecondaryContainer = SunsetPrimary,
    background = SunsetBg,
    onBackground = Color(0xFFFFF1F2),
    surface = SunsetSurface,
    onSurface = Color(0xFFFFF1F2),
    surfaceVariant = Color(0xFF381B26),
    onSurfaceVariant = Color(0xFFFDA4AF),
    outline = Color(0xFF4C2433),
    error = HangupRed,
    onError = Color.White
)

// 8. Royal Amethyst
private val AmethystColorScheme = darkColorScheme(
    primary = AmethystPrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF5B21B6),
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondary = Color(0xFFC084FC),
    onSecondary = Color.Black,
    secondaryContainer = AmethystSurface,
    onSecondaryContainer = AmethystPrimary,
    background = AmethystBg,
    onBackground = Color(0xFFF5F3FF),
    surface = AmethystSurface,
    onSurface = Color(0xFFF5F3FF),
    surfaceVariant = Color(0xFF2C1E45),
    onSurfaceVariant = Color(0xFFDDD6FE),
    outline = Color(0xFF3E2B60),
    error = HangupRed,
    onError = Color.White
)

// 9. Amber Gold
private val AmberColorScheme = darkColorScheme(
    primary = AmberPrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF78350F),
    onPrimaryContainer = Color(0xFFFEF3C7),
    secondary = Color(0xFFFBBF24),
    onSecondary = Color.Black,
    secondaryContainer = AmberSurface,
    onSecondaryContainer = AmberPrimary,
    background = AmberBg,
    onBackground = Color(0xFFFFFBEB),
    surface = AmberSurface,
    onSurface = Color(0xFFFFFBEB),
    surfaceVariant = Color(0xFF362812),
    onSurfaceVariant = Color(0xFFFDE68A),
    outline = Color(0xFF4D3B1C),
    error = HangupRed,
    onError = Color.White
)

// 10. Slate Monochrome
private val SlateColorScheme = darkColorScheme(
    primary = SlatePrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF334155),
    onPrimaryContainer = Color(0xFFF1F5F9),
    secondary = Color(0xFF94A3B8),
    onSecondary = Color.Black,
    secondaryContainer = SlateSurface,
    onSecondaryContainer = SlatePrimary,
    background = SlateBg,
    onBackground = Color(0xFFF8FAFC),
    surface = SlateSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF28282E),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF3A3A44),
    error = HangupRed,
    onError = Color.White
)

// 11. Clean Light
private val CleanLightColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E40AF),
    secondary = Color(0xFF0D9488),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF115E59),
    tertiary = WarningAmber,
    background = CanvasLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight,
    error = HangupRed,
    onError = Color.White
)

// 12. Emerald Mint Light
private val MintLightColorScheme = lightColorScheme(
    primary = DialEmeraldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCFCE7),
    onPrimaryContainer = Color(0xFF14532D),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = WarningAmber,
    background = Color(0xFFF0FDF4),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFDCFCE7),
    onSurfaceVariant = Color(0xFF334155),
    outline = Color(0xFFBBF7D0),
    error = HangupRed,
    onError = Color.White
)

// 13. Warm Paper Ivory
private val WarmPaperColorScheme = lightColorScheme(
    primary = Color(0xFFD97706),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = Color(0xFF2563EB),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDBEAFE),
    onSecondaryContainer = Color(0xFF1E40AF),
    tertiary = DialEmeraldDark,
    background = Color(0xFFFFFBEB),
    onBackground = Color(0xFF1C1917),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1917),
    surfaceVariant = Color(0xFFFEF3C7),
    onSurfaceVariant = Color(0xFF44403C),
    outline = Color(0xFFFDE68A),
    error = HangupRed,
    onError = Color.White
)

// 14. Sunset Coral Light
private val CoralLightColorScheme = lightColorScheme(
    primary = SunsetPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE4E6),
    onPrimaryContainer = Color(0xFF9F1239),
    secondary = Color(0xFF7C3AED),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF5B21B6),
    tertiary = WarningAmber,
    background = Color(0xFFFFF1F2),
    onBackground = Color(0xFF1E1B4B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1E1B4B),
    surfaceVariant = Color(0xFFFFE4E6),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFFECDD3),
    error = HangupRed,
    onError = Color.White
)

fun getDialerColorScheme(themeId: String, isDark: Boolean = true): ColorScheme {
    if (!isDark) {
        return when (themeId) {
            "clean_light" -> CleanLightColorScheme
            "pixel_light", "pixel_dark" -> PixelLightColorScheme
            "mint_light", "emerald" -> MintLightColorScheme
            "warm_paper", "amber" -> WarmPaperColorScheme
            "coral_light", "sunset" -> CoralLightColorScheme
            "cyber_cyan" -> CleanLightColorScheme
            "pitch_black", "obsidian", "slate", "amethyst" -> CleanLightColorScheme
            else -> CleanLightColorScheme
        }
    }

    return when (themeId) {
        "clean_light" -> CleanLightColorScheme
        "pixel_light" -> PixelLightColorScheme
        "mint_light" -> ForestColorScheme
        "warm_paper" -> AmberColorScheme
        "coral_light" -> SunsetColorScheme
        "obsidian" -> ObsidianColorScheme
        "pixel_dark" -> PixelDarkColorScheme
        "cyber_cyan" -> CyberCyanColorScheme
        "emerald" -> ForestColorScheme
        "sunset" -> SunsetColorScheme
        "amethyst" -> AmethystColorScheme
        "amber" -> AmberColorScheme
        "slate" -> SlateColorScheme
        "pitch_black" -> PitchBlackColorScheme
        else -> PitchBlackColorScheme
    }
}

@Composable
fun MyApplicationTheme(
    themeId: String = "pitch_black",
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = getDialerColorScheme(themeId, darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

