package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.model.AccentColor
import com.example.model.AppStyleTheme
import com.example.model.ColorMode

/**
 * CompositionLocal providing whether the active theme is Dark or Light.
 */
val LocalIsDarkTheme = staticCompositionLocalOf { true }

// --- RETAINED EXISTING SCHEMES (EXACT - DO NOT ALTER DARK MODE) ---
// (Existing) Dark mode + rock now = (new) dark + yellow accent
val RockDarkColorScheme = darkColorScheme(
    primary = RockDarkPrimary,
    onPrimary = Color(0xFF121212),
    primaryContainer = Color(0xFF2A2A2A),
    onPrimaryContainer = RockDarkPrimary,
    secondary = RockDarkSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF261233),
    onSecondaryContainer = Color(0xFFF3C4FF),
    tertiary = RockDarkTertiary,
    onTertiary = Color.White,
    background = RockDarkBackground,
    onBackground = RockDarkText,
    surface = RockDarkSurface,
    onSurface = RockDarkText,
    surfaceVariant = RockDarkSurfaceVariant,
    onSurfaceVariant = RockDarkTextSecondary,
    outline = VibrantDarkBorder,
    outlineVariant = Color(0xFF383838)
)

val RockLightColorScheme = lightColorScheme(
    primary = Color(0xFF5E6D00),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF4F8E8),
    onPrimaryContainer = Color(0xFF3B4400),
    secondary = RockLightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3D5FF),
    onSecondaryContainer = Color(0xFF380054),
    tertiary = RockLightTertiary,
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

// (Existing) Dark mode + pop now = (new) dark + blue accent
val PopDarkColorScheme = darkColorScheme(
    primary = PopDarkPrimary,
    onPrimary = Color(0xFF003824),
    primaryContainer = Color(0xFF1E293B),
    onPrimaryContainer = PopDarkPrimary,
    secondary = PopDarkSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF381222),
    onSecondaryContainer = Color(0xFFFFD9E2),
    tertiary = PopDarkTertiary,
    onTertiary = Color(0xFF121212),
    background = PopDarkBackground,
    onBackground = PopDarkText,
    surface = PopDarkSurface,
    onSurface = PopDarkText,
    surfaceVariant = PopDarkSurfaceVariant,
    onSurfaceVariant = PopDarkTextSecondary,
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF334155)
)

val PopLightColorScheme = lightColorScheme(
    primary = PopLightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC7F8E5),
    onPrimaryContainer = Color(0xFF003824),
    secondary = PopLightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFD8E4),
    onSecondaryContainer = Color(0xFF5B0024),
    tertiary = PopLightTertiary,
    onTertiary = Color.White,
    background = PopLightBackground,
    onBackground = PopLightText,
    surface = PopLightSurface,
    onSurface = PopLightText,
    surfaceVariant = PopLightSurfaceVariant,
    onSurfaceVariant = PopLightTextSecondary,
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

// --- ADDITIONAL ACCENT SCHEMES (DARK) ---
private val OrangeDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF6D00),
    onPrimary = Color(0xFF121212),
    primaryContainer = Color(0xFF2A1C14),
    onPrimaryContainer = Color(0xFFFF9E40),
    secondary = Color(0xFFFFB74D),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF331C0D),
    onSecondaryContainer = Color(0xFFFFCC80),
    tertiary = VibrantPink,
    onTertiary = Color.White,
    background = Color(0xFF14110E),
    onBackground = VibrantTextWhite,
    surface = Color(0xFF1C1714),
    onSurface = VibrantTextWhite,
    surfaceVariant = Color(0xFF2B221D),
    onSurfaceVariant = Color(0xFFB0A29A),
    outline = Color(0xFF483A32),
    outlineVariant = Color(0xFF382E28)
)

private val PurpleDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD05CE3),
    onPrimary = Color(0xFF121212),
    primaryContainer = Color(0xFF2A1435),
    onPrimaryContainer = Color(0xFFEDA5FF),
    secondary = VibrantCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF261233),
    onSecondaryContainer = Color(0xFFF3C4FF),
    tertiary = VibrantVolt,
    onTertiary = Color(0xFF121212),
    background = Color(0xFF130E17),
    onBackground = VibrantTextWhite,
    surface = Color(0xFF1E1424),
    onSurface = VibrantTextWhite,
    surfaceVariant = Color(0xFF2E1E38),
    onSurfaceVariant = Color(0xFFAFA0B8),
    outline = Color(0xFF4C335A),
    outlineVariant = Color(0xFF382542)
)

private val GreenDarkColorScheme = darkColorScheme(
    primary = Color(0xFF00E676),
    onPrimary = Color(0xFF003814),
    primaryContainer = Color(0xFF10281A),
    onPrimaryContainer = Color(0xFF69F0AE),
    secondary = VibrantVolt,
    onSecondary = Color(0xFF121212),
    secondaryContainer = Color(0xFF183321),
    onSecondaryContainer = Color(0xFFB9F6CA),
    tertiary = VibrantCyan,
    onTertiary = Color(0xFF121212),
    background = Color(0xFF0C140E),
    onBackground = VibrantTextWhite,
    surface = Color(0xFF132017),
    onSurface = VibrantTextWhite,
    surfaceVariant = Color(0xFF1E3225),
    onSurfaceVariant = Color(0xFF9EBAA7),
    outline = Color(0xFF35523F),
    outlineVariant = Color(0xFF263D2E)
)

private val RedDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF2A4B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF331016),
    onPrimaryContainer = Color(0xFFFF8093),
    secondary = Color(0xFFFF9100),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF381215),
    onSecondaryContainer = Color(0xFFFFCDD2),
    tertiary = VibrantVolt,
    onTertiary = Color(0xFF121212),
    background = Color(0xFF140D0E),
    onBackground = VibrantTextWhite,
    surface = Color(0xFF211416),
    onSurface = VibrantTextWhite,
    surfaceVariant = Color(0xFF321E21),
    onSurfaceVariant = Color(0xFFBCA1A5),
    outline = Color(0xFF553238),
    outlineVariant = Color(0xFF3E2428)
)

private val WhiteDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),
    onPrimary = Color(0xFF121212),
    primaryContainer = Color(0xFF2A2A2A),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFB0B0B0),
    onSecondary = Color(0xFF121212),
    secondaryContainer = Color(0xFF383838),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFFE0E0E0),
    onTertiary = Color(0xFF121212),
    background = Color(0xFF121212),
    onBackground = VibrantTextWhite,
    surface = Color(0xFF1C1C1C),
    onSurface = VibrantTextWhite,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFAAAAAA),
    outline = Color(0xFF484848),
    outlineVariant = Color(0xFF333333)
)

// --- ADDITIONAL ACCENT SCHEMES (LIGHT - OPTIMIZED FOR CRISP LEGIBILITY ACROSS ALL ACCENTS) ---
private val BlueLightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),          // Sky-600: vibrant oceanic azure, clear on light
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE), // Sky-100
    onPrimaryContainer = Color(0xFF0369A1), // Sky-700
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = Color(0xFF0F766E),
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),       // Crisp slate-50
    onBackground = Color(0xFF0F172A),     // Crisp slate-900 high contrast
    surface = Color.White,
    onSurface = Color(0xFF0F172A),        // Crisp slate-900 high contrast
    surfaceVariant = Color(0xFFF1F5F9),   // Slate-100 container for cards & pills
    onSurfaceVariant = Color(0xFF475569), // Slate-600 readable secondary text
    outline = Color(0xFFCBD5E1),          // Slate-300
    outlineVariant = Color(0xFFE2E8F0)    // Slate-200
)

private val OrangeLightColorScheme = lightColorScheme(
    primary = Color(0xFFEA580C),          // Orange-600: punchy, high-contrast orange
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEDD5), // Orange-100
    onPrimaryContainer = Color(0xFF9A3412), // Orange-800
    secondary = Color(0xFFEA580C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEDD5),
    onSecondaryContainer = Color(0xFF9A3412),
    tertiary = Color(0xFF78350F),
    onTertiary = Color.White,
    background = Color(0xFFFAF7F5),       // Warm off-white
    onBackground = Color(0xFF1C1917),     // Stone-900
    surface = Color.White,
    onSurface = Color(0xFF1C1917),
    surfaceVariant = Color(0xFFF5F5F4),   // Stone-100
    onSurfaceVariant = Color(0xFF57534E), // Stone-600
    outline = Color(0xFFD6D3D1),          // Stone-300
    outlineVariant = Color(0xFFE7E5E4)    // Stone-200
)

private val PurpleLightColorScheme = lightColorScheme(
    primary = Color(0xFF9333EA),          // Purple-600: rich electric violet
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3E8FF), // Purple-100
    onPrimaryContainer = Color(0xFF6B21A8), // Purple-800
    secondary = Color(0xFF9333EA),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E8FF),
    onSecondaryContainer = Color(0xFF6B21A8),
    tertiary = Color(0xFFBE185D),
    onTertiary = Color.White,
    background = Color(0xFFFAFAFC),
    onBackground = Color(0xFF111827),     // Gray-900
    surface = Color.White,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF3F4F6),   // Gray-100
    onSurfaceVariant = Color(0xFF4B5563), // Gray-600
    outline = Color(0xFFD1D5DB),          // Gray-300
    outlineVariant = Color(0xFFE5E7EB)    // Gray-200
)

private val GreenLightColorScheme = lightColorScheme(
    primary = Color(0xFF16A34A),          // Green-600: deep vibrant emerald
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCFCE7), // Green-100
    onPrimaryContainer = Color(0xFF166534), // Green-800
    secondary = Color(0xFF16A34A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCFCE7),
    onSecondaryContainer = Color(0xFF166534),
    tertiary = Color(0xFF0F766E),
    onTertiary = Color.White,
    background = Color(0xFFF8FAF9),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF0FDF4),   // Subtle mint-tinted clean container
    onSurfaceVariant = Color(0xFF374151),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

private val RedLightColorScheme = lightColorScheme(
    primary = Color(0xFFE11D48),          // Rose-600: bold, energetic crimson
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE4E6), // Rose-100
    onPrimaryContainer = Color(0xFF9F1239), // Rose-800
    secondary = Color(0xFFE11D48),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE4E6),
    onSecondaryContainer = Color(0xFF9F1239),
    tertiary = Color(0xFFC2410C),
    onTertiary = Color.White,
    background = Color(0xFFFAF9F9),
    onBackground = Color(0xFF1C1917),
    surface = Color.White,
    onSurface = Color(0xFF1C1917),
    surfaceVariant = Color(0xFFF5F5F4),
    onSurfaceVariant = Color(0xFF57534E),
    outline = Color(0xFFD6D3D1),
    outlineVariant = Color(0xFFE7E5E4)
)

private val BlackLightColorScheme = lightColorScheme(
    primary = Color(0xFF0F172A),          // Slate-900: sharp black/dark slate
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E8F0), // Slate-200
    onPrimaryContainer = Color(0xFF0F172A),
    secondary = Color(0xFF334155),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = Color(0xFF1E293B),
    tertiary = Color(0xFF475569),
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Color(0xFFE2E8F0)
)

@Composable
fun ShredSheetsTheme(
    accentColor: AccentColor = AccentColor.YELLOW,
    colorMode: ColorMode = ColorMode.DARK,
    styleTheme: AppStyleTheme? = null,
    content: @Composable () -> Unit
) {
    val isDark = when (colorMode) {
        ColorMode.DARK -> true
        ColorMode.LIGHT -> false
        ColorMode.SYSTEM -> isSystemInDarkTheme()
    }

    // Support legacy styleTheme if explicitly passed
    val baseAccent = when {
        styleTheme == AppStyleTheme.POP -> AccentColor.BLUE
        styleTheme == AppStyleTheme.ROCK && accentColor == AccentColor.YELLOW -> AccentColor.YELLOW
        else -> accentColor
    }

    // Safety checks:
    // Light mode: Yellow & White unavailable -> fallback to Blue
    // Dark mode: Black unavailable -> fallback to Yellow
    val effectiveAccent = when {
        isDark && !baseAccent.darkAllowed -> AccentColor.YELLOW
        !isDark && !baseAccent.lightAllowed -> AccentColor.BLUE
        else -> baseAccent
    }

    val colorScheme = if (isDark) {
        when (effectiveAccent) {
            AccentColor.YELLOW -> RockDarkColorScheme // Retained existing Dark + Rock
            AccentColor.BLUE -> PopDarkColorScheme     // Retained existing Dark + Pop
            AccentColor.ORANGE -> OrangeDarkColorScheme
            AccentColor.PURPLE -> PurpleDarkColorScheme
            AccentColor.GREEN -> GreenDarkColorScheme
            AccentColor.RED -> RedDarkColorScheme
            AccentColor.WHITE -> WhiteDarkColorScheme
            AccentColor.BLACK -> RockDarkColorScheme
        }
    } else {
        when (effectiveAccent) {
            AccentColor.BLUE -> BlueLightColorScheme
            AccentColor.ORANGE -> OrangeLightColorScheme
            AccentColor.PURPLE -> PurpleLightColorScheme
            AccentColor.GREEN -> GreenLightColorScheme
            AccentColor.RED -> RedLightColorScheme
            AccentColor.BLACK -> BlackLightColorScheme
            AccentColor.YELLOW -> BlueLightColorScheme
            AccentColor.WHITE -> BlueLightColorScheme
        }
    }

    CompositionLocalProvider(LocalIsDarkTheme provides isDark) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

