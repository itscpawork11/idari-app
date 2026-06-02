package com.example.domain.usecase

import com.example.data.local.LocaleManager
import com.example.domain.model.Budget
import com.example.domain.repository.BudgetRepository
import com.example.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class BudgetStatus(
    val budget: Budget,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColorHex: String,
    val percentage: Float,
    val isExceeded: Boolean,
    val remainingAmount: Double
)

class GetBudgetStatusUseCase(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) {
    fun execute(language: String = "en"): Flow<List<BudgetStatus>> {
        return combine(
            budgetRepository.getAllBudgets(),
            categoryRepository.getAllCategories()
        ) { budgets, categories ->
            budgets.map { budget ->
                val category = budget.categoryId?.let { catId ->
                    categories.find { it.id == catId }
                }

                val categoryName = category?.name ?: LocaleManager.getGeneralBudgetName(language)
                val categoryIcon = category?.iconRes ?: "other"
                val categoryColorHex = category?.colorHex ?: "#1E293B"

                val percentage = if (budget.limitAmount > 0) {
                    (budget.spentAmount / budget.limitAmount).toFloat()
                } else {
                    0f
                }

                BudgetStatus(
                    budget = budget,
                    categoryName = categoryName,
                    categoryIcon = categoryIcon,
                    categoryColorHex = categoryColorHex,
                    percentage = percentage,
                    isExceeded = budget.spentAmount > budget.limitAmount,
                    remainingAmount = budget.limitAmount - budget.spentAmount
                )
            }
        }
    }
}
