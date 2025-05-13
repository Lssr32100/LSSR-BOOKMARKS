package com.bookmarkmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bookmarkmanager.data.model.Subcategory
import com.bookmarkmanager.data.model.relations.SubcategoryWithBookmarks
import kotlinx.coroutines.flow.Flow

@Dao
interface SubcategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubcategory(subcategory: Subcategory): Long
    
    @Update
    suspend fun updateSubcategory(subcategory: Subcategory)
    
    @Delete
    suspend fun deleteSubcategory(subcategory: Subcategory)
    
    @Query("DELETE FROM subcategories WHERE id = :subcategoryId")
    suspend fun deleteSubcategoryById(subcategoryId: Int)
    
    @Query("SELECT * FROM subcategories ORDER BY name ASC")
    fun getAllSubcategories(): Flow<List<Subcategory>>
    
    @Query("SELECT * FROM subcategories WHERE category_id = :categoryId ORDER BY name ASC")
    fun getSubcategoriesByCategory(categoryId: Int): Flow<List<Subcategory>>
    
    @Query("SELECT * FROM subcategories WHERE id = :subcategoryId")
    suspend fun getSubcategoryById(subcategoryId: Int): Subcategory?
    
    @Transaction
    @Query("SELECT * FROM subcategories")
    fun getSubcategoriesWithBookmarks(): Flow<List<SubcategoryWithBookmarks>>
    
    @Transaction
    @Query("SELECT * FROM subcategories WHERE id = :subcategoryId")
    fun getSubcategoryWithBookmarks(subcategoryId: Int): Flow<SubcategoryWithBookmarks>
    
    @Transaction
    @Query("SELECT * FROM subcategories WHERE category_id = :categoryId")
    fun getSubcategoriesWithBookmarksByCategory(categoryId: Int): Flow<List<SubcategoryWithBookmarks>>
}