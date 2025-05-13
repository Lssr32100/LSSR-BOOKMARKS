package com.bookmarkmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bookmarkmanager.data.model.Category
import com.bookmarkmanager.data.model.relations.CategoryWithSubcategories
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long
    
    @Update
    suspend fun updateCategory(category: Category)
    
    @Delete
    suspend fun deleteCategory(category: Category)
    
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?
    
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>
    
    @Transaction
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    fun getCategoryWithSubcategories(categoryId: Long): Flow<CategoryWithSubcategories>
    
    @Transaction
    @Query("SELECT * FROM categories")
    fun getAllCategoriesWithSubcategories(): Flow<List<CategoryWithSubcategories>>
    
    @Query("SELECT * FROM categories")
    suspend fun getAllCategoriesOnce(): List<Category>
    
    @Transaction
    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()
}
