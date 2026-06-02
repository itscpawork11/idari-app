package com.example.domain.usecase

import com.example.domain.model.TransactionType
import com.example.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MonthlySummary(
    val monthKey: String,
    val monthName: String,
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double
)

class GetMonthlyExpensesUseCase(
    private val transactionRepository: TransactionRepository
) {
    fun execute(language: String = "en"): Flow<List<MonthlySummary>> {
        return transactionRepository.getAllTransactions().map { transactions ->
            val sdfKey = SimpleDateFormat("yyyy-MM", Locale.US)
            val locale = if (language == "ar") Locale("ar") else Locale.US
            val sdfMonth = SimpleDateFormat("MMMM yyyy", locale)

            val grouped = transactions.groupBy { sdfKey.format(Date(it.timestamp)) }

            grouped.map { (key, list) ->
                val totalIncome = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val totalExpense = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                val date = try {
                    sdfKey.parse(key) ?: Date()
                } catch (e: Exception) {
                    Date()
                }

                MonthlySummary(
                    monthKey = key,
                    monthName = sdfMonth.format(date),
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    balance = totalIncome - totalExpense
                )
            }.sortedByDescending { it.monthKey }
        }
    }
}
