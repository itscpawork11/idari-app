package com.example.domain.model

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Int,
    val timestamp: Long,
    val note: String? = null
)
