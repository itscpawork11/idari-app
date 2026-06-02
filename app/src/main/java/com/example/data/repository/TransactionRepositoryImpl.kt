package com.example.data.repository

import com.example.data.local.TransactionDao
import com.example.data.local.TransactionEntity
import com.example.data.local.TransactionWithCategoryEntity
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.model.TransactionWithCategory
import com.example.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<TransactionWithCategory>> {
        return transactionDao.getTransactionsWithCategory().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<TransactionWithCategory>> {
        return transactionDao.getTransactionsWithCategoryByDateRange(start, end).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun insertTransaction(transaction: Transaction): Long {
        return transactionDao.insertTransaction(transaction.toEntity())
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(id: Long): Boolean {
        return transactionDao.deleteTransactionById(id) > 0
    }

    override suspend fun clearAllTransactions() {
        transactionDao.deleteAllTransactions()
    }

    private fun TransactionWithCategoryEntity.toDomain() = TransactionWithCategory(
        id = id,
        amount = amount,
        type = if (type == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE,
        categoryId = categoryId,
        timestamp = timestamp,
        note = note,
        categoryName = categoryName ?: categoryIcon ?: "other",
        categoryIcon = categoryIcon ?: "other",
        categoryColorHex = categoryColorHex ?: "#95A5A6"
    )

    private fun Transaction.toEntity() = TransactionEntity(
        id = id,
        amount = amount,
        type = type.name,
        categoryId = categoryId,
        timestamp = timestamp,
        note = note
    )
}
