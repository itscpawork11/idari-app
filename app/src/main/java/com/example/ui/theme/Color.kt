package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.lightColorScheme

object FinanceColors {
    // === Theme 1: Ocean Glass (Blue) ===
    object Ocean {
        val Primary = Color(0xFF1A73E8)
        val OnPrimary = Color(0xFFFFFFFF)
        val PrimaryContainer = Color(0xFFD3E3FD)
        val OnPrimaryContainer = Color(0xFF001D36)
        val Secondary = Color(0xFF5F6B8A)
        val OnSecondary = Color(0xFFFFFFFF)
        val SecondaryContainer = Color(0xFFDBE1F5)
        val OnSecondaryContainer = Color(0xFF101C2B)
        val Tertiary = Color(0xFF785A9A)
        val OnTertiary = Color(0xFFFFFFFF)
        val TertiaryContainer = Color(0xFFF0DBFF)
        val OnTertiaryContainer = Color(0xFF251431)
        val Background = Color(0xFFF1F6FF)
        val OnBackground = Color(0xFF1A1C20)
        val Surface = Color(0xFFFFFEFE)
        val OnSurface = Color(0xFF1A1C20)
        val SurfaceVariant = Color(0xFFE2E9F7)
        val OnSurfaceVariant = Color(0xFF44474E)
        val Outline = Color(0xFFBEC6D6)
        val OutlineVariant = Color(0xFFD6DEEE)
        val Error = Color(0xFFBA1A1A)
        val OnError = Color(0xFFFFFFFF)
        val IncomeGreen = Color(0xFF1B8A3D)
        val ExpenseRed = Color(0xFFD32F2F)
    }

    // === Theme 2: Emerald Glass (Green) ===
    object Emerald {
        val Primary = Color(0xFF0F9D58)
        val OnPrimary = Color(0xFFFFFFFF)
        val PrimaryContainer = Color(0xFFC8F0D5)
        val OnPrimaryContainer = Color(0xFF00210C)
        val Secondary = Color(0xFF4D6355)
        val OnSecondary = Color(0xFFFFFFFF)
        val SecondaryContainer = Color(0xFFCBE8D3)
        val OnSecondaryContainer = Color(0xFF0A1F14)
        val Tertiary = Color(0xFF3D6473)
        val OnTertiary = Color(0xFFFFFFFF)
        val TertiaryContainer = Color(0xFFBEE7F8)
        val OnTertiaryContainer = Color(0xFF001F29)
        val Background = Color(0xFFF0FFF5)
        val OnBackground = Color(0xFF1A1C1A)
        val Surface = Color(0xFFFFFEFD)
        val OnSurface = Color(0xFF1A1C1A)
        val SurfaceVariant = Color(0xFFE0EDE3)
        val OnSurfaceVariant = Color(0xFF424941)
        val Outline = Color(0xFFBCC8BE)
        val OutlineVariant = Color(0xFFD5E1D7)
        val Error = Color(0xFFBA1A1A)
        val OnError = Color(0xFFFFFFFF)
        val IncomeGreen = Color(0xFF1B8A3D)
        val ExpenseRed = Color(0xFFC62828)
    }

    // === Theme 3: Royal Glass (Purple) ===
    object Royal {
        val Primary = Color(0xFF7C3AED)
        val OnPrimary = Color(0xFFFFFFFF)
        val PrimaryContainer = Color(0xFFEDE0FF)
        val OnPrimaryContainer = Color(0xFF1E0040)
        val Secondary = Color(0xFF655B70)
        val OnSecondary = Color(0xFFFFFFFF)
        val SecondaryContainer = Color(0xFFEBDFF8)
        val OnSecondaryContainer = Color(0xFF201929)
        val Tertiary = Color(0xFF7F525D)
        val OnTertiary = Color(0xFFFFFFFF)
        val TertiaryContainer = Color(0xFFFFD9E0)
        val OnTertiaryContainer = Color(0xFF31101B)
        val Background = Color(0xFFF8F0FF)
        val OnBackground = Color(0xFF1C1B1E)
        val Surface = Color(0xFFFFFEFE)
        val OnSurface = Color(0xFF1C1B1E)
        val SurfaceVariant = Color(0xFFECE2F2)
        val OnSurfaceVariant = Color(0xFF4A454E)
        val Outline = Color(0xFFC8C0D0)
        val OutlineVariant = Color(0xFFDDD4E4)
        val Error = Color(0xFFBA1A1A)
        val OnError = Color(0xFFFFFFFF)
        val IncomeGreen = Color(0xFF1B8A3D)
        val ExpenseRed = Color(0xFFD32F2F)
    }
}

val ThemeOceanColorScheme = lightColorScheme(
    primary = FinanceColors.Ocean.Primary,
    onPrimary = FinanceColors.Ocean.OnPrimary,
    primaryContainer = FinanceColors.Ocean.PrimaryContainer,
    onPrimaryContainer = FinanceColors.Ocean.OnPrimaryContainer,
    secondary = FinanceColors.Ocean.Secondary,
    onSecondary = FinanceColors.Ocean.OnSecondary,
    secondaryContainer = FinanceColors.Ocean.SecondaryContainer,
    onSecondaryContainer = FinanceColors.Ocean.OnSecondaryContainer,
    tertiary = FinanceColors.Ocean.Tertiary,
    onTertiary = FinanceColors.Ocean.OnTertiary,
    tertiaryContainer = FinanceColors.Ocean.TertiaryContainer,
    onTertiaryContainer = FinanceColors.Ocean.OnTertiaryContainer,
    background = FinanceColors.Ocean.Background,
    onBackground = FinanceColors.Ocean.OnBackground,
    surface = FinanceColors.Ocean.Surface,
    onSurface = FinanceColors.Ocean.OnSurface,
    surfaceVariant = FinanceColors.Ocean.SurfaceVariant,
    onSurfaceVariant = FinanceColors.Ocean.OnSurfaceVariant,
    outline = FinanceColors.Ocean.Outline,
    outlineVariant = FinanceColors.Ocean.OutlineVariant,
    error = FinanceColors.Ocean.Error,
    onError = FinanceColors.Ocean.OnError,
)

val ThemeEmeraldColorScheme = lightColorScheme(
    primary = FinanceColors.Emerald.Primary,
    onPrimary = FinanceColors.Emerald.OnPrimary,
    primaryContainer = FinanceColors.Emerald.PrimaryContainer,
    onPrimaryContainer = FinanceColors.Emerald.OnPrimaryContainer,
    secondary = FinanceColors.Emerald.Secondary,
    onSecondary = FinanceColors.Emerald.OnSecondary,
    secondaryContainer = FinanceColors.Emerald.SecondaryContainer,
    onSecondaryContainer = FinanceColors.Emerald.OnSecondaryContainer,
    tertiary = FinanceColors.Emerald.Tertiary,
    onTertiary = FinanceColors.Emerald.OnTertiary,
    tertiaryContainer = FinanceColors.Emerald.TertiaryContainer,
    onTertiaryContainer = FinanceColors.Emerald.OnTertiaryContainer,
    background = FinanceColors.Emerald.Background,
    onBackground = FinanceColors.Emerald.OnBackground,
    surface = FinanceColors.Emerald.Surface,
    onSurface = FinanceColors.Emerald.OnSurface,
    surfaceVariant = FinanceColors.Emerald.SurfaceVariant,
    onSurfaceVariant = FinanceColors.Emerald.OnSurfaceVariant,
    outline = FinanceColors.Emerald.Outline,
    outlineVariant = FinanceColors.Emerald.OutlineVariant,
    error = FinanceColors.Emerald.Error,
    onError = FinanceColors.Emerald.OnError,
)

val ThemeRoyalColorScheme = lightColorScheme(
    primary = FinanceColors.Royal.Primary,
    onPrimary = FinanceColors.Royal.OnPrimary,
    primaryContainer = FinanceColors.Royal.PrimaryContainer,
    onPrimaryContainer = FinanceColors.Royal.OnPrimaryContainer,
    secondary = FinanceColors.Royal.Secondary,
    onSecondary = FinanceColors.Royal.OnSecondary,
    secondaryContainer = FinanceColors.Royal.SecondaryContainer,
    onSecondaryContainer = FinanceColors.Royal.OnSecondaryContainer,
    tertiary = FinanceColors.Royal.Tertiary,
    onTertiary = FinanceColors.Royal.OnTertiary,
    tertiaryContainer = FinanceColors.Royal.TertiaryContainer,
    onTertiaryContainer = FinanceColors.Royal.OnTertiaryContainer,
    background = FinanceColors.Royal.Background,
    onBackground = FinanceColors.Royal.OnBackground,
    surface = FinanceColors.Royal.Surface,
    onSurface = FinanceColors.Royal.OnSurface,
    surfaceVariant = FinanceColors.Royal.SurfaceVariant,
    onSurfaceVariant = FinanceColors.Royal.OnSurfaceVariant,
    outline = FinanceColors.Royal.Outline,
    outlineVariant = FinanceColors.Royal.OutlineVariant,
    error = FinanceColors.Royal.Error,
    onError = FinanceColors.Royal.OnError,
)

fun getIncomeColor(themeIndex: Int) = when (themeIndex) {
    0 -> FinanceColors.Ocean.IncomeGreen
    1 -> FinanceColors.Emerald.IncomeGreen
    else -> FinanceColors.Royal.IncomeGreen
}

fun getExpenseColor(themeIndex: Int) = when (themeIndex) {
    0 -> FinanceColors.Ocean.ExpenseRed
    1 -> FinanceColors.Emerald.ExpenseRed
    else -> FinanceColors.Royal.ExpenseRed
}
