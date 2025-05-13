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
    
    @Query("SELECT * FROM subcategories WHERE id = :id")
    suspend fun getSubcategoryById(id: Long): Subcategory?
    
    @Query("SELECT * FROM subcategories WHERE categoryId = :categoryId ORDER BY name ASC")
    fun getSubcategoriesByCategoryId(categoryId: Long): Flow<List<Subcategory>>
    
    @Query("SELECT * FROM subcategories ORDER BY name ASC")
    fun getAllSubcategories(): Flow<List<Subcategory>>
    
    @Transaction
    @Query("SELECT * FROM subcategories WHERE id = :subcategoryId")
    fun getSubcategoryWithBookmarks(subcategoryId: Long): Flow<SubcategoryWithBookmarks>
    
    @Query("SELECT * FROM subcategories")
    suspend fun getAllSubcategoriesOnce(): List<Subcategory>
    
    @Transaction
    @Query("DELETE FROM subcategories")
    suspend fun deleteAllSubcategories()
}
