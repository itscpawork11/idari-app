package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val categoryId: Int, // Refers to CategoryEntity.id
    val timestamp: Long, // Time of transaction
    val note: String? = null
)
