package com.example.domain.usecase

import com.example.data.local.LocaleManager
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.repository.BudgetRepository
import com.example.domain.repository.CategoryRepository
import com.example.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first

data class AddTransactionResult(
    val insertedId: Long,
    val isBudgetExceeded: Boolean = false,
    val exceededCategoryName: String? = null
)

class AddTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) {
    suspend fun execute(transaction: Transaction, language: String = "en"): AddTransactionResult {
        if (transaction.amount <= 0.0) {
            throw IllegalArgumentException(LocaleManager.getValidationAmountPositive(language))
        }

        val id = transactionRepository.insertTransaction(transaction)

        budgetRepository.recalculateBudgetsSpent()

        var isBudgetExceeded = false
        var exceededCategoryName: String? = null

        if (transaction.type == TransactionType.EXPENSE) {
            val categoryBudget = budgetRepository.getBudgetByCategoryId(transaction.categoryId)
            if (categoryBudget != null) {
                if (categoryBudget.spentAmount + transaction.amount > categoryBudget.limitAmount) {
                    isBudgetExceeded = true
                    val category = categoryRepository.getCategoryById(transaction.categoryId)
                    exceededCategoryName = category?.name ?: LocaleManager.getDedicatedBudgetName(language)
                }
            }

            val generalBudget = budgetRepository.getGeneralBudget()
            if (generalBudget != null) {
                if (generalBudget.spentAmount + transaction.amount > generalBudget.limitAmount) {
                    isBudgetExceeded = true
                    exceededCategoryName = LocaleManager.getGeneralBudgetName(language)
                }
            }
        }

        return AddTransactionResult(
            insertedId = id,
            isBudgetExceeded = isBudgetExceeded,
            exceededCategoryName = exceededCategoryName
        )
    }
}
