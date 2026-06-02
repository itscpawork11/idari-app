package com.example.data.repository

import com.example.data.local.CategoryDao
import com.example.data.local.CategoryEntity
import com.example.data.local.LocaleManager
import com.example.domain.model.Category
import com.example.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getCategoryById(id: Int): Category? {
        return categoryDao.getCategoryById(id)?.toDomain()
    }

    override suspend fun addCategory(category: Category): Long {
        return categoryDao.insertCategory(category.toEntity())
    }

    override suspend fun seedDefaultCategories() {
        val defaults = listOf(
            CategoryEntity(name = "Salary & Income", iconRes = "income", colorHex = "#2ECC71"),
            CategoryEntity(name = "Food & Groceries", iconRes = "food", colorHex = "#E67E22"),
            CategoryEntity(name = "Transportation", iconRes = "transport", colorHex = "#3498DB"),
            CategoryEntity(name = "Rent & Bills", iconRes = "home", colorHex = "#E74C3C"),
            CategoryEntity(name = "Shopping", iconRes = "shopping", colorHex = "#9B59B6"),
            CategoryEntity(name = "Health & Education", iconRes = "education", colorHex = "#1ABC9C"),
            CategoryEntity(name = "Entertainment", iconRes = "entertainment", colorHex = "#F1C40F"),
            CategoryEntity(name = "Other Expenses", iconRes = "other", colorHex = "#95A5A6")
        )
        categoryDao.insertCategories(defaults)
    }

    fun getLocalizedName(categoryName: String, language: String): String {
        return LocaleManager.getDefaultCategoryName(categoryName, language)
    }

    private fun CategoryEntity.toDomain() = Category(
        id = id,
        name = name,
        iconRes = iconRes,
        colorHex = colorHex
    )

    private fun Category.toEntity() = CategoryEntity(
        id = id,
        name = name,
        iconRes = iconRes,
        colorHex = colorHex
    )
}
