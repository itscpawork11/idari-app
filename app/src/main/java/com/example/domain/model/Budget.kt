package com.example.domain.model

data class Budget(
    val id: Int = 0,
    val categoryId: Int?,
    val limitAmount: Double,
    val spentAmount: Double,
    val startDate: Long,
    val endDate: Long
)
