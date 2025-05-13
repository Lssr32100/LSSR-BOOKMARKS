package com.bookmarkmanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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
    
    @Query("SELECT * FROM bookmarks WHERE id = :id")
    suspend fun getBookmarkById(id: Long): Bookmark?
    
    @Query("SELECT * FROM bookmarks ORDER BY name ASC")
    fun getAllBookmarksOrderByNameAsc(): Flow<List<Bookmark>>
    
    @Query("SELECT * FROM bookmarks ORDER BY name DESC")
    fun getAllBookmarksOrderByNameDesc(): Flow<List<Bookmark>>
    
    @Query("SELECT * FROM bookmarks ORDER BY categoryId, subcategoryId")
    fun getAllBookmarksOrderByCategory(): Flow<List<Bookmark>>
    
    @Query("SELECT * FROM bookmarks ORDER BY subcategoryId")
    fun getAllBookmarksOrderBySubcategory(): Flow<List<Bookmark>>
    
    @Query("SELECT * FROM bookmarks ORDER BY type")
    fun getAllBookmarksOrderByType(): Flow<List<Bookmark>>
    
    @Query("SELECT * FROM bookmarks WHERE categoryId = :categoryId")
    fun getBookmarksByCategory(categoryId: Long): Flow<List<Bookmark>>
    
    @Query("SELECT * FROM bookmarks WHERE subcategoryId = :subcategoryId")
    fun getBookmarksBySubcategory(subcategoryId: Long): Flow<List<Bookmark>>
    
    @Query("SELECT * FROM bookmarks WHERE type = :type")
    fun getBookmarksByType(type: BookmarkType): Flow<List<Bookmark>>
    
    @Query("SELECT * FROM bookmarks WHERE name LIKE '%' || :query || '%' OR " +
           "id IN (SELECT b.id FROM bookmarks b JOIN categories c ON b.categoryId = c.id WHERE c.name LIKE '%' || :query || '%') OR " +
           "id IN (SELECT b.id FROM bookmarks b JOIN subcategories s ON b.subcategoryId = s.id WHERE s.name LIKE '%' || :query || '%')")
    fun searchBookmarks(query: String): Flow<List<Bookmark>>
    
    @Query("SELECT * FROM bookmarks")
    suspend fun getAllBookmarksOnce(): List<Bookmark>
    
    @Transaction
    @Query("DELETE FROM bookmarks")
    suspend fun deleteAllBookmarks()
}
