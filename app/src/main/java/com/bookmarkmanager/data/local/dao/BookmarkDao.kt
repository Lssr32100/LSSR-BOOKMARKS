package com.bookmarkmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bookmarkmanager.data.model.Bookmark
import com.bookmarkmanager.data.model.BookmarkType
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark): Long
    
    @Update
    suspend fun updateBookmark(bookmark: Bookmark)
    
    @Delete
    suspend fun deleteBookmark(bookmark: Bookmark)
    
    @Query("DELETE FROM bookmarks WHERE id = :bookmarkId")
    suspend fun deleteBookmarkById(bookmarkId: Int)
    
    @Query("SELECT * FROM bookmarks ORDER BY name ASC")
    fun getAllBookmarks(): Flow<List<Bookmark>>
    
    @Query("SELECT * FROM bookmarks WHERE id = :bookmarkId")
    suspend fun getBookmarkById(bookmarkId: Int): Bookmark?
    
    @Query("SELECT * FROM bookmarks WHERE category_id = :categoryId")
    fun getBookmarksByCategory(categoryId: Int): Flow<List<Bookmark>>
    
    @Query("SELECT * FROM bookmarks WHERE subcategory_id = :subcategoryId")
    fun getBookmarksBySubcategory(subcategoryId: Int): Flow<List<Bookmark>>
    
    @Query("SELECT * FROM bookmarks WHERE type = :type")
    fun getBookmarksByType(type: BookmarkType): Flow<List<Bookmark>>
    
    @Query("SELECT * FROM bookmarks WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchBookmarks(query: String): Flow<List<Bookmark>>
    
    @Query("""
        SELECT b.* FROM bookmarks b
        JOIN categories c ON b.category_id = c.id
        JOIN subcategories s ON b.subcategory_id = s.id
        WHERE b.name LIKE '%' || :query || '%' 
        OR b.description LIKE '%' || :query || '%'
        OR c.name LIKE '%' || :query || '%'
        OR s.name LIKE '%' || :query || '%'
    """)
    fun searchBookmarksWithCategoryAndSubcategory(query: String): Flow<List<Bookmark>>
}