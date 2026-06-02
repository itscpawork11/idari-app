package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.LocaleManager
import com.example.domain.model.Budget
import com.example.domain.model.Category
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.model.TransactionWithCategory
import com.example.domain.repository.BudgetRepository
import com.example.domain.repository.CategoryRepository
import com.example.domain.repository.TransactionRepository
import com.example.domain.usecase.AddTransactionUseCase
import com.example.domain.usecase.BudgetStatus
import com.example.domain.usecase.GetBudgetStatusUseCase
import com.example.domain.usecase.GetMonthlyExpensesUseCase
import com.example.domain.usecase.MonthlySummary
import com.example.R
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseViewModel(
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getMonthlyExpensesUseCase: GetMonthlyExpensesUseCase,
    private val getBudgetStatusUseCase: GetBudgetStatusUseCase
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<ExpenseEvent>()
    val eventFlow: SharedFlow<ExpenseEvent> = _eventFlow.asSharedFlow()

    private val _currency = MutableStateFlow("ج.م")
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(false)
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _isUnlocked = MutableStateFlow(true)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _isBalanceVisible = MutableStateFlow(false)
    val isBalanceVisible: StateFlow<Boolean> = _isBalanceVisible.asStateFlow()

    private val _isTransactionsVisible = MutableStateFlow(false)
    val isTransactionsVisible: StateFlow<Boolean> = _isTransactionsVisible.asStateFlow()

    private val _themeIndex = MutableStateFlow(0)
    val themeIndex: StateFlow<Int> = _themeIndex.asStateFlow()

    private var prefs: android.content.SharedPreferences? = null

    val budgets: StateFlow<List<Budget>> = budgetRepository.getAllBudgets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = categoryRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionWithCategory>> = transactionRepository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgetStatuses: StateFlow<List<BudgetStatus>> = combine(
        getBudgetStatusUseCase.execute(),
        categories,
        _language
    ) { statuses, cats, lang ->
        statuses.map { status ->
            val catName = if (status.budget.categoryId != null) {
                val cat = cats.find { it.id == status.budget.categoryId }
                cat?.let { LocaleManager.getDefaultCategoryName(it.iconRes, lang) } ?: status.categoryName
            } else {
                LocaleManager.getGeneralBudgetName(lang)
            }
            status.copy(categoryName = catName)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlySummaries: StateFlow<List<MonthlySummary>> = combine(
        getMonthlyExpensesUseCase.execute(),
        _language
    ) { summaries, lang ->
        val locale = if (lang == "ar") java.util.Locale("ar") else java.util.Locale.US
        val sdf = java.text.SimpleDateFormat("MMMM yyyy", locale)
        val sdfKey = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US)
        summaries.map { summary ->
            val date = try { sdfKey.parse(summary.monthKey) ?: java.util.Date() } catch (e: Exception) { java.util.Date() }
            summary.copy(monthName = sdf.format(date))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun init(context: Context) {
        prefs = context.getSharedPreferences("idari_prefs", Context.MODE_PRIVATE)
        _isBiometricEnabled.value = prefs!!.getBoolean("biometric_enabled", false)
        _language.value = prefs!!.getString("language", "en") ?: "en"
        val savedCurrency = prefs!!.getString("currency", "ج.م") ?: "ج.م"
        _currency.value = savedCurrency
        _themeIndex.value = prefs!!.getInt("theme_index", 0)
        if (_isBiometricEnabled.value) {
            _isUnlocked.value = false
        }
    }

    fun setLockState(locked: Boolean) {
        _isUnlocked.value = !locked
        if (locked) {
            _isBalanceVisible.value = false
            _isTransactionsVisible.value = false
        }
    }

    fun toggleBalanceVisibility() {
        _isBalanceVisible.value = !_isBalanceVisible.value
    }

    fun toggleTransactionsVisibility() {
        _isTransactionsVisible.value = !_isTransactionsVisible.value
    }

    fun setCurrency(newCurrency: String) {
        _currency.value = newCurrency
        prefs?.edit()?.putString("currency", newCurrency)?.apply()
    }

    fun setLanguage(lang: String) {
        _language.value = lang
        prefs?.edit()?.putString("language", lang)?.apply()
    }

    fun isRtl(): Boolean = _language.value == "ar"

    fun toggleBiometric() {
        val newVal = !_isBiometricEnabled.value
        _isBiometricEnabled.value = newVal
        prefs?.edit()?.putBoolean("biometric_enabled", newVal)?.apply()
    }

    fun setTheme(index: Int) {
        _themeIndex.value = index
        prefs?.edit()?.putInt("theme_index", index)?.apply()
    }

    fun getLocalizedCategoryName(iconRes: String): String {
        return LocaleManager.getDefaultCategoryName(iconRes, _language.value)
    }

    fun addCategory(name: String, iconRes: String, colorHex: String = "#607D8B") {
        viewModelScope.launch {
            try {
                val category = Category(
                    name = name,
                    iconRes = iconRes,
                    colorHex = colorHex
                )
                categoryRepository.addCategory(category)
                _eventFlow.emit(ExpenseEvent.CategoryCreatedSuccessfully)
            } catch (e: Exception) {
                _eventFlow.emit(ExpenseEvent.Error(R.string.error_add_category))
            }
        }
    }

    data class CategorySpending(
        val name: String,
        val amount: Double,
        val colorHex: String,
        val percentage: Float
    )

    fun addTransaction(amount: Double, type: TransactionType, categoryId: Int, note: String?) {
        viewModelScope.launch {
            try {
                val transaction = Transaction(
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    timestamp = System.currentTimeMillis(),
                    note = note
                )
                val result = addTransactionUseCase.execute(transaction, _language.value)
                if (result.isBudgetExceeded) {
                    _eventFlow.emit(ExpenseEvent.BudgetExceededWarning(result.exceededCategoryName ?: "Budget"))
                } else {
                    _eventFlow.emit(ExpenseEvent.TransactionAddedSuccessfully)
                }
            } catch (e: Exception) {
                _eventFlow.emit(ExpenseEvent.Error(R.string.error_add_transaction))
            }
        }
    }

    fun updateTransaction(id: Long, amount: Double, type: TransactionType, categoryId: Int, timestamp: Long, note: String?) {
        viewModelScope.launch {
            try {
                val transaction = Transaction(
                    id = id,
                    amount = amount,
                    type = type,
                    categoryId = categoryId,
                    timestamp = timestamp,
                    note = note
                )
                transactionRepository.updateTransaction(transaction)
                budgetRepository.recalculateBudgetsSpent()
                _eventFlow.emit(ExpenseEvent.TransactionUpdatedSuccessfully)
            } catch (e: Exception) {
                _eventFlow.emit(ExpenseEvent.Error(R.string.error_update_transaction))
            }
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            val success = transactionRepository.deleteTransaction(id)
            if (success) {
                budgetRepository.recalculateBudgetsSpent()
                _eventFlow.emit(ExpenseEvent.TransactionDeletedSuccessfully)
            } else {
                _eventFlow.emit(ExpenseEvent.Error(R.string.error_delete_transaction))
            }
        }
    }

    fun createBudget(categoryId: Int?, limitAmount: Double) {
        viewModelScope.launch {
            val budget = Budget(
                categoryId = categoryId,
                limitAmount = limitAmount,
                spentAmount = 0.0,
                startDate = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L,
                endDate = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L
            )
            budgetRepository.saveBudget(budget)
            _eventFlow.emit(ExpenseEvent.BudgetCreatedSuccessfully)
        }
    }

    fun deleteBudget(id: Int) {
        viewModelScope.launch {
            val success = budgetRepository.deleteBudget(id)
            if (success) {
                _eventFlow.emit(ExpenseEvent.BudgetDeletedSuccessfully)
            } else {
                _eventFlow.emit(ExpenseEvent.Error(R.string.error_delete_budget))
            }
        }
    }

    fun exportToCsv(context: Context) {
        viewModelScope.launch {
            try {
                val transactionsList = allTransactions.value
                val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm", Locale.US)
                val fileName = "expenses_${sdf.format(Date())}.csv"

                val cachePath = File(context.cacheDir, "exports")
                cachePath.mkdirs()
                val file = File(cachePath, fileName)

                FileOutputStream(file).use { fos ->
                    fos.write(0xEF); fos.write(0xBB); fos.write(0xBF)
                    val writer = fos.bufferedWriter()
                    writer.write("ID,Amount,Type,Category,Date,Notes\n")

                    val sdfDate = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)
                    for (t in transactionsList) {
                        val typeText = if (t.type == TransactionType.INCOME) "Income" else "Expense"
                        val noteText = t.note ?: ""
                        val formattedDate = sdfDate.format(Date(t.timestamp))
                        val catName = getLocalizedCategoryName(t.categoryIcon)
                        writer.write("${t.id},${t.amount},${typeText},${catName},${formattedDate},${noteText}\n")
                    }
                    writer.flush()
                }

                _eventFlow.emit(ExpenseEvent.ExportSuccess(file))
            } catch (e: Exception) {
                _eventFlow.emit(ExpenseEvent.Error(R.string.export_failed, e.localizedMessage))
            }
        }
    }

    fun exportBackup(context: Context) {
        viewModelScope.launch {
            try {
                val json = org.json.JSONObject().apply {
                    put("version", 1)
                    put("categories", org.json.JSONArray(categories.value.map { cat ->
                        org.json.JSONObject().apply {
                            put("id", cat.id)
                            put("name", cat.name)
                            put("iconRes", cat.iconRes)
                            put("colorHex", cat.colorHex)
                        }
                    }))
                    put("transactions", org.json.JSONArray(allTransactions.value.map { t ->
                        org.json.JSONObject().apply {
                            put("amount", t.amount)
                            put("type", t.type.name)
                            put("categoryId", t.categoryId)
                            put("timestamp", t.timestamp)
                            put("note", t.note ?: "")
                        }
                    }))
                    put("budgets", org.json.JSONArray(budgets.value.map { b ->
                        org.json.JSONObject().apply {
                            put("id", b.id)
                            put("categoryId", b.categoryId ?: org.json.JSONObject.NULL)
                            put("limitAmount", b.limitAmount)
                            put("spentAmount", b.spentAmount)
                            put("startDate", b.startDate)
                            put("endDate", b.endDate)
                        }
                    }))
                }

                val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm", Locale.US)
                val fileName = "idari_backup_${sdf.format(Date())}.json"
                val cachePath = File(context.cacheDir, "backups")
                cachePath.mkdirs()
                val file = File(cachePath, fileName)
                file.writeText(json.toString(2))

                _eventFlow.emit(ExpenseEvent.BackupSuccess(file))
            } catch (e: Exception) {
                _eventFlow.emit(ExpenseEvent.Error(R.string.error_restore_failed, e.localizedMessage))
            }
        }
    }

    fun restoreBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonStr = inputStream?.bufferedReader()?.readText() ?: throw Exception("Empty file")
                val root = org.json.JSONObject(jsonStr)
                val categoriesArray = root.getJSONArray("categories")
                val transactionsArray = root.getJSONArray("transactions")
                val budgetsArray = root.getJSONArray("budgets")

                transactionRepository.clearAllTransactions()

                for (i in 0 until budgetsArray.length()) {
                    val b = budgetsArray.getJSONObject(i)
                    val catId = if (b.isNull("categoryId")) null else b.getInt("categoryId")
                    val budget = com.example.domain.model.Budget(
                        id = b.getInt("id"),
                        categoryId = catId,
                        limitAmount = b.getDouble("limitAmount"),
                        spentAmount = b.getDouble("spentAmount"),
                        startDate = b.getLong("startDate"),
                        endDate = b.getLong("endDate")
                    )
                    budgetRepository.saveBudget(budget)
                }

                for (i in 0 until categoriesArray.length()) {
                    val c = categoriesArray.getJSONObject(i)
                    val category = com.example.domain.model.Category(
                        id = c.getInt("id"),
                        name = c.getString("name"),
                        iconRes = c.getString("iconRes"),
                        colorHex = c.getString("colorHex")
                    )
                    categoryRepository.addCategory(category)
                }

                for (i in 0 until transactionsArray.length()) {
                    val t = transactionsArray.getJSONObject(i)
                    val type = try { TransactionType.valueOf(t.getString("type")) } catch (e: Exception) { TransactionType.EXPENSE }
                    val transaction = Transaction(
                        amount = t.getDouble("amount"),
                        type = type,
                        categoryId = t.getInt("categoryId"),
                        timestamp = t.getLong("timestamp"),
                        note = t.optString("note", null)
                    )
                    transactionRepository.insertTransaction(transaction)
                }

                budgetRepository.recalculateBudgetsSpent()
                _eventFlow.emit(ExpenseEvent.RestoreSuccess)
            } catch (e: Exception) {
                _eventFlow.emit(ExpenseEvent.Error(R.string.error_restore_failed, e.localizedMessage))
            }
        }
    }

    fun requestBiometricForExport() {
        viewModelScope.launch {
            _eventFlow.emit(ExpenseEvent.RequestBiometricForExport)
        }
    }

    fun requestBiometricForClear() {
        viewModelScope.launch {
            _eventFlow.emit(ExpenseEvent.RequestBiometricForClear)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                transactionRepository.clearAllTransactions()
                budgetRepository.getAllBudgets().first().forEach { budget ->
                    budgetRepository.deleteBudget(budget.id)
                }
                _eventFlow.emit(ExpenseEvent.DataClearedSuccessfully)
            } catch (e: Exception) {
                _eventFlow.emit(ExpenseEvent.Error(R.string.error_clear_failed, e.localizedMessage))
            }
        }
    }

    sealed class ExpenseEvent {
        object TransactionAddedSuccessfully : ExpenseEvent()
        object TransactionUpdatedSuccessfully : ExpenseEvent()
        object TransactionDeletedSuccessfully : ExpenseEvent()
        object BudgetCreatedSuccessfully : ExpenseEvent()
        object BudgetDeletedSuccessfully : ExpenseEvent()
        object DataClearedSuccessfully : ExpenseEvent()
        object CategoryCreatedSuccessfully : ExpenseEvent()
        data class BudgetExceededWarning(val categoryName: String) : ExpenseEvent()
        data class ExportSuccess(val file: File) : ExpenseEvent()
        data class BackupSuccess(val file: File) : ExpenseEvent()
        object RestoreSuccess : ExpenseEvent()
        data class Error(val messageResId: Int, val formatArg: String? = null) : ExpenseEvent()
        object RequestBiometricForExport : ExpenseEvent()
        object RequestBiometricForClear : ExpenseEvent()
    }



    class Factory(
        private val categoryRepository: CategoryRepository,
        private val transactionRepository: TransactionRepository,
        private val budgetRepository: BudgetRepository,
        private val addTransactionUseCase: AddTransactionUseCase,
        private val getMonthlyExpensesUseCase: GetMonthlyExpensesUseCase,
        private val getBudgetStatusUseCase: GetBudgetStatusUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
                return ExpenseViewModel(
                    categoryRepository,
                    transactionRepository,
                    budgetRepository,
                    addTransactionUseCase,
                    getMonthlyExpensesUseCase,
                    getBudgetStatusUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
