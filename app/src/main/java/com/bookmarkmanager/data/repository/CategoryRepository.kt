package com.bookmarkmanager.data.repository

import com.bookmarkmanager.data.local.dao.CategoryDao
import com.bookmarkmanager.data.model.Category
import com.bookmarkmanager.data.model.relations.CategoryWithSubcategories
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(private val categoryDao: CategoryDao) {
    
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    
    val allCategoriesWithSubcategories: Flow<List<CategoryWithSubcategories>> = categoryDao.getCategoriesWithSubcategories()
    
    suspend fun insert(category: Category): Long {
        return categoryDao.insertCategory(category)
    }
    
    suspend fun update(category: Category) {
        categoryDao.updateCategory(category)
    }
    
    suspend fun delete(category: Category) {
        categoryDao.deleteCategory(category)
    }
    
    suspend fun deleteById(categoryId: Int) {
        categoryDao.deleteCategoryById(categoryId)
    }
    
    suspend fun getCategoryById(categoryId: Int): Category? {
        return categoryDao.getCategoryById(categoryId)
    }
    
    fun getCategoryWithSubcategories(categoryId: Int): Flow<CategoryWithSubcategories> {
        return categoryDao.getCategoryWithSubcategories(categoryId)
    }
}