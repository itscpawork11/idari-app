package com.example.domain.repository

import com.example.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    suspend fun getCategoryById(id: Int): Category?
    suspend fun addCategory(category: Category): Long
    suspend fun seedDefaultCategories()
}
