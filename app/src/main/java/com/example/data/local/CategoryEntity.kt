package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val iconRes: String, // Maps to vector icon names (e.g. "shopping", "food", "transport", "home", "income", "education", "gift", "other")
    val colorHex: String  // Hex string (e.g. "#FF0000")
)
