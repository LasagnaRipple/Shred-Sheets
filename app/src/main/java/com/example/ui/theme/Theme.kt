package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.AccentColor
import com.example.model.AppStyleTheme
import com.example.model.ColorMode

// --- RETAINED EXISTING SCHEMES (EXACT) ---
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
    primary = RockLightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8EDE0),
    onPrimaryContainer = Color(0xFF3B4400),
    secondary = RockLightSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3D5FF),
    onSecondaryContainer = Color(0xFF380054),
    tertiary = RockLightTertiary,
    onTertiary = Color.White,
    background = RockLightBackground,
    onBackground = RockLightText,
    surface = RockLightSurface,
    onSurface = RockLightText,
    surfaceVariant = RockLightSurfaceVariant,
    onSurfaceVariant = RockLightTextSecondary,
    outline = Color(0xFFCCCCCC),
    outlineVariant = Color(0xFFE0E0E0)
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

// --- ADDITIONAL ACCENT SCHEMES (LIGHT) ---
private val BlueLightColorScheme = lightColorScheme(
    primary = Color(0xFF0077B6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE1F0F8),
    onPrimaryContainer = Color(0xFF003B5C),
    secondary = Color(0xFF0288D1),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE1F5FE),
    onSecondaryContainer = Color(0xFF01579B),
    tertiary = Color(0xFF009688),
    onTertiary = Color.White,
    background = Color(0xFFF4F9FC),
    onBackground = Color(0xFF142129),
    surface = Color.White,
    onSurface = Color(0xFF142129),
    surfaceVariant = Color(0xFFE5F0F6),
    onSurfaceVariant = Color(0xFF4C616F),
    outline = Color(0xFFBBD5E5),
    outlineVariant = Color(0xFFD6E7F1)
)

private val OrangeLightColorScheme = lightColorScheme(
    primary = Color(0xFFE65100),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFECE0),
    onPrimaryContainer = Color(0xFF5C1D00),
    secondary = Color(0xFFF57C00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF3E0),
    onSecondaryContainer = Color(0xFFE65100),
    tertiary = Color(0xFF8D6E63),
    onTertiary = Color.White,
    background = Color(0xFFFFF9F5),
    onBackground = Color(0xFF281A12),
    surface = Color.White,
    onSurface = Color(0xFF281A12),
    surfaceVariant = Color(0xFFFCECE0),
    onSurfaceVariant = Color(0xFF6B584E),
    outline = Color(0xFFE8D0C0),
    outlineVariant = Color(0xFFF2E3D8)
)

private val PurpleLightColorScheme = lightColorScheme(
    primary = Color(0xFF7B1FA2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF6E8FC),
    onPrimaryContainer = Color(0xFF380054),
    secondary = Color(0xFF9C27B0),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E5F5),
    onSecondaryContainer = Color(0xFF4A148C),
    tertiary = Color(0xFFC2185B),
    onTertiary = Color.White,
    background = Color(0xFFFAF5FC),
    onBackground = Color(0xFF231429),
    surface = Color.White,
    onSurface = Color(0xFF231429),
    surfaceVariant = Color(0xFFF2E5F6),
    onSurfaceVariant = Color(0xFF644F6C),
    outline = Color(0xFFDEC5E7),
    outlineVariant = Color(0xFFEDE0F3)
)

private val GreenLightColorScheme = lightColorScheme(
    primary = Color(0xFF1B803A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE4F7EA),
    onPrimaryContainer = Color(0xFF003B14),
    secondary = Color(0xFF2E7D32),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8F5E9),
    onSecondaryContainer = Color(0xFF1B5E20),
    tertiary = Color(0xFF00796B),
    onTertiary = Color.White,
    background = Color(0xFFF4FAF5),
    onBackground = Color(0xFF132317),
    surface = Color.White,
    onSurface = Color(0xFF132317),
    surfaceVariant = Color(0xFFE0EFE4),
    onSurfaceVariant = Color(0xFF4F6855),
    outline = Color(0xFFBEDBC5),
    outlineVariant = Color(0xFFD6EBDC)
)

private val RedLightColorScheme = lightColorScheme(
    primary = Color(0xFFC62828),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFEAEA),
    onPrimaryContainer = Color(0xFF4E0606),
    secondary = Color(0xFFD32F2F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEBEE),
    onSecondaryContainer = Color(0xFFB71C1C),
    tertiary = Color(0xFFFF6F00),
    onTertiary = Color.White,
    background = Color(0xFFFFF6F6),
    onBackground = Color(0xFF271313),
    surface = Color.White,
    onSurface = Color(0xFF271313),
    surfaceVariant = Color(0xFFFCE6E6),
    onSurfaceVariant = Color(0xFF6E5151),
    outline = Color(0xFFECC2C2),
    outlineVariant = Color(0xFFF5D6D6)
)

private val BlackLightColorScheme = lightColorScheme(
    primary = Color(0xFF121212),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5E5E5),
    onPrimaryContainer = Color(0xFF121212),
    secondary = Color(0xFF424242),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEEEEE),
    onSecondaryContainer = Color(0xFF212121),
    tertiary = Color(0xFF616161),
    onTertiary = Color.White,
    background = Color(0xFFF8F9FA),
    onBackground = Color(0xFF121212),
    surface = Color.White,
    onSurface = Color(0xFF121212),
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF555555),
    outline = Color(0xFFCCCCCC),
    outlineVariant = Color(0xFFE0E0E0)
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

