package com.bookmarkmanager.data.repository

import com.bookmarkmanager.data.local.dao.SubcategoryDao
import com.bookmarkmanager.data.model.Subcategory
import com.bookmarkmanager.data.model.relations.SubcategoryWithBookmarks
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubcategoryRepository @Inject constructor(private val subcategoryDao: SubcategoryDao) {
    
    val allSubcategories: Flow<List<Subcategory>> = subcategoryDao.getAllSubcategories()
    
    val allSubcategoriesWithBookmarks: Flow<List<SubcategoryWithBookmarks>> = subcategoryDao.getSubcategoriesWithBookmarks()
    
    fun getSubcategoriesByCategory(categoryId: Int): Flow<List<Subcategory>> {
        return subcategoryDao.getSubcategoriesByCategory(categoryId)
    }
    
    fun getSubcategoriesWithBookmarksByCategory(categoryId: Int): Flow<List<SubcategoryWithBookmarks>> {
        return subcategoryDao.getSubcategoriesWithBookmarksByCategory(categoryId)
    }
    
    suspend fun insert(subcategory: Subcategory): Long {
        return subcategoryDao.insertSubcategory(subcategory)
    }
    
    suspend fun update(subcategory: Subcategory) {
        subcategoryDao.updateSubcategory(subcategory)
    }
    
    suspend fun delete(subcategory: Subcategory) {
        subcategoryDao.deleteSubcategory(subcategory)
    }
    
    suspend fun deleteById(subcategoryId: Int) {
        subcategoryDao.deleteSubcategoryById(subcategoryId)
    }
    
    suspend fun getSubcategoryById(subcategoryId: Int): Subcategory? {
        return subcategoryDao.getSubcategoryById(subcategoryId)
    }
    
    fun getSubcategoryWithBookmarks(subcategoryId: Int): Flow<SubcategoryWithBookmarks> {
        return subcategoryDao.getSubcategoryWithBookmarks(subcategoryId)
    }
}