package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryId: Int?, // Can be null for overall/general budget limit
    val limitAmount: Double,
    val spentAmount: Double,
    val startDate: Long,
    val endDate: Long
)
