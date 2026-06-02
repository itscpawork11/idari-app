package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName) {
        "income" -> Icons.AutoMirrored.Filled.TrendingUp
        "food" -> Icons.Default.Restaurant
        "transport" -> Icons.Default.DirectionsCar
        "home" -> Icons.Default.Home
        "shopping" -> Icons.Default.ShoppingCart
        "education" -> Icons.Default.School
        "entertainment" -> Icons.Default.Celebration
        else -> Icons.Default.Category
    }
}
