package com.example.domain.repository

import com.example.domain.model.Transaction
import com.example.domain.model.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<TransactionWithCategory>>
    fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<TransactionWithCategory>>
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: Long): Boolean
    suspend fun clearAllTransactions()
}
