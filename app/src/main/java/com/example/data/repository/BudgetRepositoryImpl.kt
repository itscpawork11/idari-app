package com.example.data.repository

import com.example.data.local.BudgetDao
import com.example.data.local.BudgetEntity
import com.example.data.local.TransactionDao
import com.example.domain.model.Budget
import com.example.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class BudgetRepositoryImpl(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao
) : BudgetRepository {

    override fun getAllBudgets(): Flow<List<Budget>> {
        return budgetDao.getAllBudgets().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getBudgetByCategoryId(categoryId: Int): Budget? {
        return budgetDao.getBudgetByCategoryId(categoryId)?.toDomain()
    }

    override suspend fun getGeneralBudget(): Budget? {
        return budgetDao.getGeneralBudget()?.toDomain()
    }

    override suspend fun getBudgetById(id: Int): Budget? {
        return budgetDao.getBudgetById(id)?.toDomain()
    }

    override suspend fun saveBudget(budget: Budget): Long {
        val budgetId = budgetDao.insertBudget(budget.toEntity())
        recalculateBudgetsSpent() // Maintain consistency on save
        return budgetId
    }

    override suspend fun updateSpentAmount(budgetId: Int, spentAmount: Double): Boolean {
        return budgetDao.updateSpentAmount(budgetId, spentAmount) > 0
    }

    override suspend fun deleteBudget(id: Int): Boolean {
        return budgetDao.deleteBudgetById(id) > 0
    }

    override suspend fun recalculateBudgetsSpent() {
        // Query current budgets
        val budgetsList = budgetDao.getAllBudgets().first()
        // Query transactions
        val transactionsEntities = transactionDao.getTransactionsWithCategory().first()

        for (budget in budgetsList) {
            val spentSum = transactionsEntities.filter { t ->
                val matchesCategory = budget.categoryId == null || t.categoryId == budget.categoryId
                val matchesType = t.type == "EXPENSE"
                val matchesTime = t.timestamp >= budget.startDate && t.timestamp <= budget.endDate
                matchesCategory && matchesType && matchesTime
            }.sumOf { it.amount }

            budgetDao.updateSpentAmount(budget.id, spentSum)
        }
    }

    private fun BudgetEntity.toDomain() = Budget(
        id = id,
        categoryId = categoryId,
        limitAmount = limitAmount,
        spentAmount = spentAmount,
        startDate = startDate,
        endDate = endDate
    )

    private fun Budget.toEntity() = BudgetEntity(
        id = id,
        categoryId = categoryId,
        limitAmount = limitAmount,
        spentAmount = spentAmount,
        startDate = startDate,
        endDate = endDate
    )
}
