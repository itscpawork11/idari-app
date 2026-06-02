package com.example.domain.repository

import com.example.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getAllBudgets(): Flow<List<Budget>>
    suspend fun getBudgetByCategoryId(categoryId: Int): Budget?
    suspend fun getGeneralBudget(): Budget?
    suspend fun getBudgetById(id: Int): Budget?
    suspend fun saveBudget(budget: Budget): Long
    suspend fun updateSpentAmount(budgetId: Int, spentAmount: Double): Boolean
    suspend fun deleteBudget(id: Int): Boolean
    suspend fun recalculateBudgetsSpent()
}
