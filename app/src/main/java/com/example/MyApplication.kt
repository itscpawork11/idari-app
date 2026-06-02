package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.repository.BudgetRepositoryImpl
import com.example.data.repository.CategoryRepositoryImpl
import com.example.data.repository.TransactionRepositoryImpl
import com.example.domain.repository.BudgetRepository
import com.example.domain.repository.CategoryRepository
import com.example.domain.repository.TransactionRepository
import com.example.domain.usecase.AddTransactionUseCase
import com.example.domain.usecase.GetBudgetStatusUseCase
import com.example.domain.usecase.GetMonthlyExpensesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MyApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var categoryRepository: CategoryRepository
        private set

    lateinit var transactionRepository: TransactionRepository
        private set

    lateinit var budgetRepository: BudgetRepository
        private set

    lateinit var addTransactionUseCase: AddTransactionUseCase
        private set

    lateinit var getMonthlyExpensesUseCase: GetMonthlyExpensesUseCase
        private set

    lateinit var getBudgetStatusUseCase: GetBudgetStatusUseCase
        private set

    private val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "idari_expenses_db"
        ).fallbackToDestructiveMigration().build()

        categoryRepository = CategoryRepositoryImpl(database.categoryDao())
        transactionRepository = TransactionRepositoryImpl(database.transactionDao())
        budgetRepository = BudgetRepositoryImpl(database.budgetDao(), database.transactionDao())

        addTransactionUseCase = AddTransactionUseCase(transactionRepository, budgetRepository, categoryRepository)
        getMonthlyExpensesUseCase = GetMonthlyExpensesUseCase(transactionRepository)
        getBudgetStatusUseCase = GetBudgetStatusUseCase(budgetRepository, categoryRepository)

        applicationScope.launch {
            try {
                val currentCats = categoryRepository.getAllCategories().first()
                if (currentCats.isEmpty()) {
                    categoryRepository.seedDefaultCategories()
                }
            } catch (e: Exception) {
                categoryRepository.seedDefaultCategories()
            }
        }
    }
}
