package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun MyApplicationTheme(
    themeIndex: Int = 0,
    isRtl: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (themeIndex) {
        0 -> ThemeOceanColorScheme
        1 -> ThemeEmeraldColorScheme
        else -> ThemeRoyalColorScheme
    }

    val fontFamily = getFontFamily(isRtl = isRtl)
    val typography = buildTypography(fontFamily)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
