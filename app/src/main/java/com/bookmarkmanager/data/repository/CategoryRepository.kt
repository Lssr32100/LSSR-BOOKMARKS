package com.bookmarkmanager.data.repository

import com.bookmarkmanager.data.local.dao.CategoryDao
import com.bookmarkmanager.data.model.Category
import com.bookmarkmanager.data.model.relations.CategoryWithSubcategories
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category)
    }
    
    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }
    
    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }
    
    suspend fun getCategoryById(id: Long): Category? {
        return categoryDao.getCategoryById(id)
    }
    
    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories()
    }
    
    fun getCategoryWithSubcategories(categoryId: Long): Flow<CategoryWithSubcategories> {
        return categoryDao.getCategoryWithSubcategories(categoryId)
    }
    
    fun getAllCategoriesWithSubcategories(): Flow<List<CategoryWithSubcategories>> {
        return categoryDao.getAllCategoriesWithSubcategories()
    }
    
    suspend fun getAllCategoriesOnce(): List<Category> {
        return categoryDao.getAllCategoriesOnce()
    }
    
    suspend fun deleteAllCategories() {
        categoryDao.deleteAllCategories()
    }
}
