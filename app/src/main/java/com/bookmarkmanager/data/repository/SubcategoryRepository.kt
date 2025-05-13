package com.bookmarkmanager.data.repository

import com.bookmarkmanager.data.local.dao.SubcategoryDao
import com.bookmarkmanager.data.model.Subcategory
import com.bookmarkmanager.data.model.relations.SubcategoryWithBookmarks
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubcategoryRepository @Inject constructor(
    private val subcategoryDao: SubcategoryDao
) {
    suspend fun insertSubcategory(subcategory: Subcategory): Long {
        return subcategoryDao.insertSubcategory(subcategory)
    }
    
    suspend fun updateSubcategory(subcategory: Subcategory) {
        subcategoryDao.updateSubcategory(subcategory)
    }
    
    suspend fun deleteSubcategory(subcategory: Subcategory) {
        subcategoryDao.deleteSubcategory(subcategory)
    }
    
    suspend fun getSubcategoryById(id: Long): Subcategory? {
        return subcategoryDao.getSubcategoryById(id)
    }
    
    fun getSubcategoriesByCategoryId(categoryId: Long): Flow<List<Subcategory>> {
        return subcategoryDao.getSubcategoriesByCategoryId(categoryId)
    }
    
    fun getAllSubcategories(): Flow<List<Subcategory>> {
        return subcategoryDao.getAllSubcategories()
    }
    
    fun getSubcategoryWithBookmarks(subcategoryId: Long): Flow<SubcategoryWithBookmarks> {
        return subcategoryDao.getSubcategoryWithBookmarks(subcategoryId)
    }
    
    suspend fun getAllSubcategoriesOnce(): List<Subcategory> {
        return subcategoryDao.getAllSubcategoriesOnce()
    }
    
    suspend fun deleteAllSubcategories() {
        subcategoryDao.deleteAllSubcategories()
    }
}
