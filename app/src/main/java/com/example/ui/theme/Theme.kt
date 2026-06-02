package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

val GlassCardShape = RoundedCornerShape(24.dp)
val GlassCardElevation = CardDefaults.cardElevation(defaultElevation = 4.dp)

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
