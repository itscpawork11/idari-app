package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("""
        SELECT t.id, t.amount, t.type, t.categoryId, t.timestamp, t.note, 
               c.name as categoryName, c.iconRes as categoryIcon, c.colorHex as categoryColorHex 
        FROM transactions t 
        LEFT JOIN categories c ON t.categoryId = c.id 
        ORDER BY t.timestamp DESC
    """)
    fun getTransactionsWithCategory(): Flow<List<TransactionWithCategoryEntity>>

    @Query("""
        SELECT t.id, t.amount, t.type, t.categoryId, t.timestamp, t.note, 
               c.name as categoryName, c.iconRes as categoryIcon, c.colorHex as categoryColorHex 
        FROM transactions t 
        LEFT JOIN categories c ON t.categoryId = c.id 
        WHERE t.timestamp >= :start AND t.timestamp <= :end
        ORDER BY t.timestamp DESC
    """)
    fun getTransactionsWithCategoryByDateRange(start: Long, end: Long): Flow<List<TransactionWithCategoryEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long): Int

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}
