package com.example.data.local

data class TransactionWithCategoryEntity(
    val id: Long,
    val amount: Double,
    val type: String,
    val categoryId: Int,
    val timestamp: Long,
    val note: String?,
    val categoryName: String?,
    val categoryIcon: String?,
    val categoryColorHex: String?
)
