package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Auxiliary Gold Accents for Holy items
val IslamicGold = Color(0xFFF59E0B)
val IslamicGoldLight = Color(0xFFFCD34D)
val IslamicGoldDark = Color(0xFFB45309)

val IslamicGreenPrimary = Color(0xFF14B8A6)
val IslamicGreenDark = Color(0xFF121417)
val IslamicGreenLight = Color(0xFF2DD4BF)

val GoldGradientStart = Color(0xFFFCD34D)
val GoldGradientEnd = Color(0xFFD97706)

val GreenGradientStart = Color(0xFF1E293B)
val GreenGradientEnd = Color(0xFF0F172A)

data class AppThemeColors(
    val bg: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val border: Color,
    val borderSubtle: Color,
    val textMain: Color,
    val textTitle: Color,
    val textMuted: Color,
    val textSubtle: Color,
    val tealAccent: Color,
    val tealAccentLight: Color,
    val tealGlow10: Color,
    val tealGlow20: Color,
    val tealGlow40: Color,
    val heroGradientStart: Color,
    val heroGradientEnd: Color,
    val bottomNavBg: Color,
    val isDark: Boolean
)

// Royal Dark Palette (Gold & Charcoal Black)
val RoyalDarkAppThemeColors = AppThemeColors(
    bg = Color(0xFF0F1115),
    surface = Color(0xFF16191E),
    surfaceVariant = Color(0xFF20242B),
    border = Color(0x80D4AF37),
    borderSubtle = Color(0x35D4AF37),
    textMain = Color(0xFFF2F4F7),
    textTitle = Color(0xFFFFFFFF),
    textMuted = Color(0xFFDCDFE5),
    textSubtle = Color(0xFFA0A5B0),
    tealAccent = Color(0xFFD4AF37),
    tealAccentLight = Color(0xFFF0D28B),
    tealGlow10 = Color(0x1AD4AF37),
    tealGlow20 = Color(0x33D4AF37),
    tealGlow40 = Color(0x66D4AF37),
    heroGradientStart = Color(0xFF262116),
    heroGradientEnd = Color(0xFF14120D),
    bottomNavBg = Color(0xFF16191E),
    isDark = true
)

// Emerald Dark Palette (Forest Emerald Green & Gold)
val EmeraldDarkAppThemeColors = AppThemeColors(
    bg = Color(0xFF071B12),
    surface = Color(0xFF0D281C),
    surfaceVariant = Color(0xFF133827),
    border = Color(0xFF235540),
    borderSubtle = Color(0xFF184231),
    textMain = Color(0xFFE8F5E9),
    textTitle = Color(0xFFFFFFFF),
    textMuted = Color(0xFFA3C9B8),
    textSubtle = Color(0xFF6E9985),
    tealAccent = Color(0xFF52B788),
    tealAccentLight = Color(0xFF74C69D),
    tealGlow10 = Color(0x1A52B788),
    tealGlow20 = Color(0x3352B788),
    tealGlow40 = Color(0x6652B788),
    heroGradientStart = Color(0xFF163C2B),
    heroGradientEnd = Color(0xFF0D281C),
    bottomNavBg = Color(0xFF0D281C),
    isDark = true
)

// Ice Blue Light Palette (Soft Sky Blue Pastel & Navy)
val IceBlueLightAppThemeColors = AppThemeColors(
    bg = Color(0xFFE8EFF8),
    surface = Color(0xFFF4F7FC),
    surfaceVariant = Color(0xFFD3E1F2),
    border = Color(0xFF8CAACF),
    borderSubtle = Color(0xFFB8D0E6),
    textMain = Color(0xFF0F1D30),
    textTitle = Color(0xFF0B1D2C),
    textMuted = Color(0xFF2C4366),
    textSubtle = Color(0xFF5A7394),
    tealAccent = Color(0xFF2B5282),
    tealAccentLight = Color(0xFF3B629B),
    tealGlow10 = Color(0x1A2B5282),
    tealGlow20 = Color(0x332B5282),
    tealGlow40 = Color(0x662B5282),
    heroGradientStart = Color(0xFFD6E6F7),
    heroGradientEnd = Color(0xFFC2DCF2),
    bottomNavBg = Color(0xFFF4F7FC),
    isDark = false
)

val DarkAppThemeColors = RoyalDarkAppThemeColors
val LightAppThemeColors = IceBlueLightAppThemeColors

val LocalAppThemeColors = staticCompositionLocalOf { IceBlueLightAppThemeColors }

object AppColors {
    val current: AppThemeColors
        @Composable
        get() = LocalAppThemeColors.current
}

@Composable
fun PrayerTimesTheme(
    themeMode: AppThemeMode = ThemeManager.currentThemeMode,
    content: @Composable () -> Unit,
) {
    val appThemeColors = when (themeMode) {
        AppThemeMode.ROYAL_DARK -> RoyalDarkAppThemeColors
        AppThemeMode.EMERALD_DARK -> EmeraldDarkAppThemeColors
        AppThemeMode.ICE_BLUE_LIGHT -> IceBlueLightAppThemeColors
    }

    val colorScheme = when (themeMode) {
        AppThemeMode.ROYAL_DARK -> RoyalDarkColorScheme
        AppThemeMode.EMERALD_DARK -> DarkEmeraldColorScheme
        AppThemeMode.ICE_BLUE_LIGHT -> LightIceBlueColorScheme
    }

    CompositionLocalProvider(LocalAppThemeColors provides appThemeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun PrayerTimesTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    val currentMode = if (darkTheme) {
        if (ThemeManager.currentThemeMode == AppThemeMode.EMERALD_DARK) AppThemeMode.EMERALD_DARK else AppThemeMode.ROYAL_DARK
    } else {
        AppThemeMode.ICE_BLUE_LIGHT
    }
    PrayerTimesTheme(themeMode = currentMode, content = content)
}
